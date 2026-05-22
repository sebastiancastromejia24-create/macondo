package com.macondo.jewelry.Controller.dto.request;

import com.macondo.jewelry.Entity.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.Set;

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
