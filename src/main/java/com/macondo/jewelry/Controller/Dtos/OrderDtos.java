package com.macondo.jewelry.Controller.Dtos;


import com.macondo.jewelry.Entity.OrderStatus;
import com.macondo.jewelry.Entity.ShippingAddress;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import java.time.Instant;

public final class OrderDtos {
    private OrderDtos() {
    }

    public record CreateOrderRequest(@NotNull Long productId, @Valid ShippingAddressRequest shippingAddress) {
    }

    public record ShippingAddressRequest(
            @NotBlank String recipientName,
            @NotBlank String city,
            @NotBlank String addressLine,
            @NotBlank String phone
    ) {
    }

    public record UpdateOrderStatusRequest(@NotNull OrderStatus status) {
    }

    public record OrderResponse(
            Long id,
            String reference,
            Long productId,
            String productName,
            String shippingCity,
            String shippingAddressLine,
            long productAmountCents,
            long wompiCommissionCents,
            long totalAmountCents,
            OrderStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}
