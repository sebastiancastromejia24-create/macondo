package com.macondo.jewelry.Controller.Dtos;


import com.macondo.jewelry.Entity.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.Set;

public final class ProductDtos {
    private ProductDtos() {
    }

    public record ProductRequest(
            @NotBlank String name,
            String description,
            @Positive long priceCents,
            String imageUrl,
            Long categoryId,
            Set<Long> materialIds,
            ProductStatus status
    ) {
    }

    public record ProductResponse(
            Long id,
            String name,
            String description,
            long priceCents,
            String imageUrl,
            Long categoryId,
            String categoryName,
            Set<String> materialNames,
            ProductStatus status,
            Instant reservedUntil,
            long version
    ) {
    }
}
