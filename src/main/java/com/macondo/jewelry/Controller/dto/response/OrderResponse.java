package com.macondo.jewelry.Controller.dto.response;

import com.macondo.jewelry.Entity.OrderStatus;
import java.time.Instant;

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
