package com.macondo.jewelry.order;

import com.macondo.jewelry.product.Product;
import com.macondo.jewelry.product.ProductService;
import com.macondo.jewelry.user.AppUser;
import jakarta.persistence.EntityNotFoundException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final SecureRandom secureRandom = new SecureRandom();

    public OrderService(OrderRepository orderRepository, ProductService productService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
    }

    @Transactional
    public CustomerOrder createPendingOrder(AppUser user, Long productId, long commissionCents, long totalCents) {
        Product product = productService.reserve(productId);
        CustomerOrder order = new CustomerOrder(
                nextReference(),
                user,
                product,
                product.getPriceCents(),
                commissionCents,
                totalCents
        );
        return orderRepository.save(order);
    }

    @Transactional
    public CustomerOrder createPendingOrder(AppUser user, Long productId) {
        Product product = productService.reserve(productId);
        CustomerOrder order = new CustomerOrder(nextReference(), user, product, product.getPriceCents(), 0, product.getPriceCents());
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<CustomerOrder> history(AppUser user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<CustomerOrder> adminOrders(OrderStatus status) {
        if (status == null) {
            return orderRepository.findAllByOrderByCreatedAtDesc();
        }
        return orderRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Transactional(readOnly = true)
    public CustomerOrder findOwned(String reference, AppUser user) {
        CustomerOrder order = orderRepository.findByReference(reference)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes ver pedidos de otro usuario");
        }
        return order;
    }

    @Transactional
    public CustomerOrder updateShippingStatus(Long id, OrderStatus status) {
        CustomerOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));
        if (status != OrderStatus.SHIPPED && status != OrderStatus.DELIVERED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se permite actualizar a SHIPPED o DELIVERED");
        }
        order.updateStatus(status);
        return order;
    }

    private String nextReference() {
        byte[] bytes = new byte[8];
        secureRandom.nextBytes(bytes);
        return "MAC-" + HexFormat.of().formatHex(bytes).toUpperCase();
    }
}
