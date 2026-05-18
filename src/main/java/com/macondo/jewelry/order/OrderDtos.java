package com.macondo.jewelry.order;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public final class OrderDtos {
    private OrderDtos() {
    }

    public record CreateOrderRequest(@NotNull Long productId) {
    }

    public record UpdateOrderStatusRequest(@NotNull OrderStatus status) {
    }

    public record OrderResponse(
            Long id,
            String reference,
            Long productId,
            String productName,
            long productAmountCents,
            long wompiCommissionCents,
            long totalAmountCents,
            OrderStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static OrderResponse from(CustomerOrder order) {
            return new OrderResponse(
                    order.getId(),
                    order.getReference(),
                    order.getProduct().getId(),
                    order.getProduct().getName(),
                    order.getProductAmountCents(),
                    order.getWompiCommissionCents(),
                    order.getTotalAmountCents(),
                    order.getStatus(),
                    order.getCreatedAt(),
                    order.getUpdatedAt()
            );
        }
    }
}
