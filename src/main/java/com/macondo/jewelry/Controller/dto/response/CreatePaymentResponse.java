package com.macondo.jewelry.Controller.dto.response;

import com.macondo.jewelry.Entity.OrderStatus;

public record CreatePaymentResponse(
        String publicKey,
        String currency,
        String reference,
        long amountInCents,
        String integritySignature,
        PaymentBreakdown breakdown,
        OrderStatus orderStatus
) {
}
