package com.macondo.jewelry.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.macondo.jewelry.order.CustomerOrder;
import com.macondo.jewelry.order.OrderRepository;
import com.macondo.jewelry.order.OrderService;
import com.macondo.jewelry.order.OrderStatus;
import com.macondo.jewelry.product.Product;
import com.macondo.jewelry.product.ProductService;
import com.macondo.jewelry.user.AppUser;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PaymentService {
    private final WompiProperties properties;
    private final WompiSignatureService signatureService;
    private final ProductService productService;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;
    private final PaymentEmailService emailService;

    public PaymentService(
            WompiProperties properties,
            WompiSignatureService signatureService,
            ProductService productService,
            OrderService orderService,
            OrderRepository orderRepository,
            ObjectMapper objectMapper,
            PaymentEmailService emailService
    ) {
        this.properties = properties;
        this.signatureService = signatureService;
        this.productService = productService;
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.objectMapper = objectMapper;
        this.emailService = emailService;
    }

    @Transactional
    public PaymentDtos.CreatePaymentResponse createPayment(AppUser user, Long productId) {
        Product product = productService.find(productId);
        long commissionCents = calculateCommission(product.getPriceCents());
        long totalCents = product.getPriceCents() + commissionCents;
        CustomerOrder order = orderService.createPendingOrder(user, productId, commissionCents, totalCents);
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
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Firma de webhook invalida");
        }
        WebhookTransaction transaction = readTransaction(rawBody);
        CustomerOrder order = orderRepository.findByReference(transaction.reference())
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));
        if (order.isWebhookProcessed()) {
            return;
        }
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
            String reference = transaction.path("reference").asText(null);
            String status = transaction.path("status").asText("PENDING");
            if (reference == null || reference.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Webhook sin referencia");
            }
            return new WebhookTransaction(reference, PaymentStatus.valueOf(status));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado de pago invalido");
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Webhook invalido");
        }
    }

    private record WebhookTransaction(String reference, PaymentStatus status) {
    }
}
