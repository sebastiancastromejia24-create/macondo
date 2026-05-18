package com.macondo.jewelry.product;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStatus(ProductStatus status);

    @Modifying
    @Query("update Product p set p.status = 'AVAILABLE', p.reservedUntil = null where p.status = 'RESERVED' and p.reservedUntil < :now")
    int releaseExpiredReservations(Instant now);
}
