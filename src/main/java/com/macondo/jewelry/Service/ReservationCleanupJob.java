package com.macondo.jewelry.Service;


import com.macondo.jewelry.Entity.OrderStatus;
import com.macondo.jewelry.Entity.Product;
import com.macondo.jewelry.Repository.OrderRepository;
import com.macondo.jewelry.Repository.ProductRepository;
import com.macondo.jewelry.Entity.ProductStatus;
import java.time.Instant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ReservationCleanupJob {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public ReservationCleanupJob(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Scheduled(fixedDelayString = "${app.reservations.cleanup-delay-ms:60000}")
    @Transactional
    public void releaseExpiredReservations() {
        Instant now = Instant.now();
        productRepository.findByStatusAndReservedUntilBefore(ProductStatus.RESERVED, now).forEach(product -> {
            orderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.PENDING_PAYMENT).stream()
                    .filter(order -> order.getProduct().getId().equals(product.getId()))
                    .forEach(order -> order.updateStatus(OrderStatus.CANCELLED));
            product.release();
        });
    }
}
