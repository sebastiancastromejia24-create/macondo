package com.macondo.jewelry.Service;

import com.macondo.jewelry.Entity.PaymentStatus;
import com.macondo.jewelry.Entity.PaymentTransaction;
import com.macondo.jewelry.Integration.PaymentEmailService;
import com.macondo.jewelry.Integration.WompiProperties;
import com.macondo.jewelry.Integration.WompiSignatureService;
import com.macondo.jewelry.Repository.PaymentTransactionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.macondo.jewelry.Controller.dto.request.ShippingAddressRequest;
import com.macondo.jewelry.Controller.dto.response.CreatePaymentResponse;
import com.macondo.jewelry.Controller.dto.response.PaymentBreakdown;
import com.macondo.jewelry.Common.BusinessException;
import com.macondo.jewelry.Common.ResourceNotFoundException;
import com.macondo.jewelry.Entity.CustomerOrder;
import com.macondo.jewelry.Repository.OrderRepository;
import com.macondo.jewelry.Entity.OrderStatus;
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
    public CreatePaymentResponse createPayment(AppUser user, Long productId, ShippingAddressRequest shippingAddress) {
        Product product = productService.find(productId);
        long commissionCents = calculateCommission(product.getPriceCents());
        long totalCents = product.getPriceCents() + commissionCents;
        CustomerOrder order = orderService.createPendingOrder(user, productId, shippingAddress, commissionCents, totalCents);
        String signature = signatureService.integritySignature(order.getReference(), order.getTotalAmountCents());
        return new CreatePaymentResponse(
                properties.publicKey(),
                properties.currency(),
                order.getReference(),
                order.getTotalAmountCents(),
                signature,
                new PaymentBreakdown(order.getProductAmountCents(), order.getWompiCommissionCents(), order.getTotalAmountCents()),
                order.getStatus()
        );
    }

    @Transactional
    public void processWebhook(String rawBody, String signature) {
        JsonNode root = readWebhookRoot(rawBody);
        if (!signatureService.isValidWebhookSignature(root, signature)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Firma de webhook invalida");
        }
        WebhookTransaction transaction = readTransaction(root);
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
        if (status == PaymentStatus.DECLINED || status == PaymentStatus.VOIDED || status == PaymentStatus.ERROR) {
            order.updateStatus(OrderStatus.FAILED_PAYMENT);
            order.getProduct().release();
        }
    }

    private long calculateCommission(long priceCents) {
        BigDecimal variable = BigDecimal.valueOf(priceCents).multiply(properties.commissionRate());
        return variable.setScale(0, RoundingMode.HALF_UP).longValue() + properties.fixedFeeCents();
    }

    private JsonNode readWebhookRoot(String rawBody) {
        try {
            return objectMapper.readTree(rawBody);
        } catch (Exception ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Webhook invalido");
        }
    }

    private WebhookTransaction readTransaction(JsonNode root) {
        try {
            JsonNode transaction = root.path("data").path("transaction");
            String id = transaction.path("id").asText(null);
            String reference = transaction.path("reference").asText(null);
            String status = transaction.path("status").asText("PENDING");
            if (reference == null || reference.isBlank()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Webhook sin referencia");
            }
            id = (id == null || id.isBlank() ? reference : id) + "-" + status;
            return new WebhookTransaction(id, reference, PaymentStatus.valueOf(status), amountInCents(transaction));
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Estado de pago invalido");
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Webhook invalido");
        }
    }

    private long amountInCents(JsonNode transaction) {
        if (transaction.has("amount_in_cents")) {
            return transaction.path("amount_in_cents").asLong(0);
        }
        return transaction.path("amountInCents").asLong(0);
    }

    private record WebhookTransaction(String id, String reference, PaymentStatus status, long amountInCents) {
    }
}
