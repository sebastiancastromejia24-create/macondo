package com.macondo.jewelry.order;

import com.macondo.jewelry.user.AppUser;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
    Optional<CustomerOrder> findByReference(String reference);

    List<CustomerOrder> findByUserOrderByCreatedAtDesc(AppUser user);

    List<CustomerOrder> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    List<CustomerOrder> findAllByOrderByCreatedAtDesc();

    @Modifying
    @Query("""
            update CustomerOrder o
            set o.status = 'CANCELLED', o.updatedAt = :now
            where o.status = 'PENDING_PAYMENT'
              and o.product.id in (
                select p.id from Product p where p.status = 'RESERVED' and p.reservedUntil < :now
              )
            """)
    int cancelExpiredReservations(Instant now);
}
