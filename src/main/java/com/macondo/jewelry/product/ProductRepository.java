package com.macondo.jewelry.product;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStatus(ProductStatus status);

    List<Product> findByStatusAndReservedUntilBefore(ProductStatus status, Instant now);
}
