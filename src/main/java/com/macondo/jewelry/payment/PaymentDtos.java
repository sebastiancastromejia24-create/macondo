package com.macondo.jewelry.payment;

import com.macondo.jewelry.order.OrderStatus;
import jakarta.validation.constraints.NotNull;

public final class PaymentDtos {
    private PaymentDtos() {
    }

    public record CreatePaymentRequest(@NotNull Long productId) {
    }

    public record PaymentBreakdown(long productValueCents, long wompiCommissionCents, long totalCents) {
    }

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
}
