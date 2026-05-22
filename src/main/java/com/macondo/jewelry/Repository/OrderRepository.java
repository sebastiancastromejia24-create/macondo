package com.macondo.jewelry.Repository;


import com.macondo.jewelry.Entity.CustomerOrder;
import com.macondo.jewelry.Entity.OrderStatus;
import com.macondo.jewelry.Entity.AppUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
    Optional<CustomerOrder> findByReference(String reference);

    List<CustomerOrder> findByUserOrderByCreatedAtDesc(AppUser user);

    List<CustomerOrder> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    List<CustomerOrder> findAllByOrderByCreatedAtDesc();
}
