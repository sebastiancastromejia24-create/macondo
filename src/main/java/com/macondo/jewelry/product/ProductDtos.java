package com.macondo.jewelry.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.time.Instant;

public final class ProductDtos {
    private ProductDtos() {
    }

    public record ProductRequest(
            @NotBlank String name,
            String description,
            @Positive long priceCents,
            String imageUrl,
            ProductStatus status
    ) {
    }

    public record ProductResponse(
            Long id,
            String name,
            String description,
            long priceCents,
            String imageUrl,
            ProductStatus status,
            Instant reservedUntil,
            long version
    ) {
        public static ProductResponse from(Product product) {
            return new ProductResponse(
                    product.getId(),
                    product.getName(),
                    product.getDescription(),
                    product.getPriceCents(),
                    product.getImageUrl(),
                    product.getStatus(),
                    product.getReservedUntil(),
                    product.getVersion()
            );
        }
    }
}
