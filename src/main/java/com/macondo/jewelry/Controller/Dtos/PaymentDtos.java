package com.macondo.jewelry.Controller.Dtos;


import com.macondo.jewelry.Entity.ShippingAddress;
import com.macondo.jewelry.Entity.OrderStatus;
import com.macondo.jewelry.Controller.Dtos.OrderDtos.ShippingAddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public final class PaymentDtos {
    private PaymentDtos() {
    }

    public record CreatePaymentRequest(@NotNull Long productId, @Valid ShippingAddressRequest shippingAddress) {
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
