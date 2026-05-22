package com.macondo.jewelry.Controller;


import com.macondo.jewelry.Controller.Dtos.PaymentDtos;
import com.macondo.jewelry.Entity.ShippingAddress;
import com.macondo.jewelry.Service.PaymentService;
import com.macondo.jewelry.Controller.Dtos.PaymentDtos.CreatePaymentRequest;
import com.macondo.jewelry.Controller.Dtos.PaymentDtos.CreatePaymentResponse;
import com.macondo.jewelry.Security.AuthenticatedUser;
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
@RequestMapping("/api/v1/pagos")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/crear")
    @ResponseStatus(HttpStatus.CREATED)
    public CreatePaymentResponse create(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody CreatePaymentRequest request) {
        return paymentService.createPayment(user.user(), request.productId(), request.shippingAddress());
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
