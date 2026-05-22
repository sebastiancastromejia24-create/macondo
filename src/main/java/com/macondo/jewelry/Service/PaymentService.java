package com.macondo.jewelry.Service;


import com.macondo.jewelry.Controller.Dtos.OrderDtos;
import com.macondo.jewelry.Controller.Dtos.PaymentDtos;
import com.macondo.jewelry.Entity.PaymentStatus;
import com.macondo.jewelry.Entity.PaymentTransaction;
import com.macondo.jewelry.Entity.ShippingAddress;
import com.macondo.jewelry.Integration.PaymentEmailService;
import com.macondo.jewelry.Integration.WompiProperties;
import com.macondo.jewelry.Integration.WompiSignatureService;
import com.macondo.jewelry.Repository.PaymentTransactionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.macondo.jewelry.Common.BusinessException;
import com.macondo.jewelry.Common.ResourceNotFoundException;
import com.macondo.jewelry.Entity.CustomerOrder;
import com.macondo.jewelry.Repository.OrderRepository;
import com.macondo.jewelry.Service.OrderService;
import com.macondo.jewelry.Entity.OrderStatus;
import com.macondo.jewelry.Controller.Dtos.OrderDtos.ShippingAddressRequest;
import com.macondo.jewelry.Entity.Product;
import com.macondo.jewelry.Entity.AppUser;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {
    private final WompiProperties properties;
    private final WompiSignatureService signatureService;
    private final ProductService productService;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final ObjectMapper objectMapper;
    private final PaymentEmailService emailService;

    public PaymentService(
            WompiProperties properties,
            WompiSignatureService signatureService,
            ProductService productService,
            OrderService orderService,
            OrderRepository orderRepository,
            PaymentTransactionRepository paymentTransactionRepository,
            ObjectMapper objectMapper,
            PaymentEmailService emailService
    ) {
        this.properties = properties;
        this.signatureService = signatureService;
        this.productService = productService;
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.objectMapper = objectMapper;
        this.emailService = emailService;
    }

    @Transactional
    public PaymentDtos.CreatePaymentResponse createPayment(AppUser user, Long productId, ShippingAddressRequest shippingAddress) {
        Product product = productService.find(productId);
        long commissionCents = calculateCommission(product.getPriceCents());
        long totalCents = product.getPriceCents() + commissionCents;
        CustomerOrder order = orderService.createPendingOrder(user, productId, shippingAddress, commissionCents, totalCents);
        String signature = signatureService.integritySignature(order.getReference(), order.getTotalAmountCents());
        return new PaymentDtos.CreatePaymentResponse(
                properties.publicKey(),
                properties.currency(),
                order.getReference(),
                order.getTotalAmountCents(),
                signature,
                new PaymentDtos.PaymentBreakdown(order.getProductAmountCents(), order.getWompiCommissionCents(), order.getTotalAmountCents()),
                order.getStatus()
        );
    }

    @Transactional
    public void processWebhook(String rawBody, String signature) {
        if (!signatureService.isValidWebhookSignature(rawBody, signature)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Firma de webhook invalida");
        }
        WebhookTransaction transaction = readTransaction(rawBody);
        if (paymentTransactionRepository.findByWompiTransactionId(transaction.id()).isPresent()) {
            return;
        }
        CustomerOrder order = orderRepository.findByReference(transaction.reference())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
        if (order.isWebhookProcessed()) {
            return;
        }
        paymentTransactionRepository.save(new PaymentTransaction(transaction.id(), order, transaction.status(), transaction.amountInCents()));
        if (transaction.status() == PaymentStatus.PENDING) {
            return;
        }
        applyPaymentStatus(order, transaction.status());
        order.markWebhookProcessed();
    }

    private void applyPaymentStatus(CustomerOrder order, PaymentStatus status) {
        if (status == PaymentStatus.APPROVED) {
            order.updateStatus(OrderStatus.APPROVED);
            order.getProduct().markSold();
            emailService.sendApprovedPaymentEmail(
                    order.getUser().getEmail(),
                    order.getUser().getName(),
                    order.getReference(),
                    order.getProduct().getName(),
                    order.getTotalAmountCents()
            );
            return;
        }
        if (status == PaymentStatus.DECLINED || status == PaymentStatus.VOIDED) {
            order.updateStatus(OrderStatus.FAILED_PAYMENT);
            order.getProduct().release();
        }
    }

    private long calculateCommission(long priceCents) {
        BigDecimal variable = BigDecimal.valueOf(priceCents).multiply(properties.commissionRate());
        return variable.setScale(0, RoundingMode.HALF_UP).longValue() + properties.fixedFeeCents();
    }

    private WebhookTransaction readTransaction(String rawBody) {
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            JsonNode transaction = root.path("data").path("transaction");
            String id = transaction.path("id").asText(null);
            String reference = transaction.path("reference").asText(null);
            String status = transaction.path("status").asText("PENDING");
            if (reference == null || reference.isBlank()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Webhook sin referencia");
            }
            id = (id == null || id.isBlank() ? reference : id) + "-" + status;
            return new WebhookTransaction(id, reference, PaymentStatus.valueOf(status), transaction.path("amount_in_cents").asLong(0));
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Estado de pago invalido");
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Webhook invalido");
        }
    }

    private record WebhookTransaction(String id, String reference, PaymentStatus status, long amountInCents) {
    }
}
