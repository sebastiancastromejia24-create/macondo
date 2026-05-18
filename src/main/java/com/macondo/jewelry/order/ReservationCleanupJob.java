package com.macondo.jewelry.order;

import com.macondo.jewelry.product.ProductRepository;
import com.macondo.jewelry.product.ProductStatus;
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
