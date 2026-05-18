package com.macondo.jewelry.payment;

import com.macondo.jewelry.payment.PaymentDtos.CreatePaymentRequest;
import com.macondo.jewelry.payment.PaymentDtos.CreatePaymentResponse;
import com.macondo.jewelry.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.Enumeration;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pagos")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/crear")
    @ResponseStatus(HttpStatus.CREATED)
    public CreatePaymentResponse create(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody CreatePaymentRequest request) {
        return paymentService.createPayment(user.user(), request.productId());
    }

    @PostMapping("/webhook")
    public void webhook(@RequestBody String rawBody, HttpServletRequest request) {
        paymentService.processWebhook(rawBody, webhookSignature(request));
    }

    private String webhookSignature(HttpServletRequest request) {
        Enumeration<String> names = request.getHeaderNames();
        for (String name : Collections.list(names)) {
            if (name.equalsIgnoreCase("x-wompi-signature") || name.equalsIgnoreCase("x-event-checksum")) {
                return request.getHeader(name);
            }
        }
        return null;
    }
}
