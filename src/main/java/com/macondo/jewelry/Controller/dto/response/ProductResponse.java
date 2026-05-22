package com.macondo.jewelry.Controller.dto.response;

import com.macondo.jewelry.Entity.ProductStatus;
import java.time.Instant;
import java.util.Set;

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
