package com.macondo.jewelry.Service;

import com.macondo.jewelry.Controller.dto.request.ShippingAddressRequest;
import com.macondo.jewelry.Entity.CustomerOrder;
import com.macondo.jewelry.Entity.OrderStatus;
import com.macondo.jewelry.Entity.ShippingAddress;
import com.macondo.jewelry.Repository.OrderRepository;
import com.macondo.jewelry.Repository.ShippingAddressRepository;
import com.macondo.jewelry.Common.BusinessException;
import com.macondo.jewelry.Common.ForbiddenOperationException;
import com.macondo.jewelry.Common.ResourceNotFoundException;
import com.macondo.jewelry.Entity.Product;
import com.macondo.jewelry.Entity.AppUser;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ShippingAddressRepository shippingAddressRepository;
    private final ProductService productService;
    private final SecureRandom secureRandom = new SecureRandom();

    public OrderService(OrderRepository orderRepository, ShippingAddressRepository shippingAddressRepository, ProductService productService) {
        this.orderRepository = orderRepository;
        this.shippingAddressRepository = shippingAddressRepository;
        this.productService = productService;
    }

    @Transactional
    public CustomerOrder createPendingOrder(AppUser user, Long productId, ShippingAddressRequest addressRequest, long commissionCents, long totalCents) {
        Product product = productService.reserve(productId);
        ShippingAddress shippingAddress = saveAddress(user, addressRequest);
        CustomerOrder order = new CustomerOrder(
                nextReference(),
                user,
                product,
                shippingAddress,
                product.getPriceCents(),
                commissionCents,
                totalCents
        );
        return orderRepository.save(order);
    }

    @Transactional
    public CustomerOrder createPendingOrder(AppUser user, Long productId, ShippingAddressRequest addressRequest) {
        Product product = productService.reserve(productId);
        CustomerOrder order = new CustomerOrder(nextReference(), user, product, saveAddress(user, addressRequest), product.getPriceCents(), 0, product.getPriceCents());
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
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new ForbiddenOperationException("No puedes ver pedidos de otro usuario");
        }
        return order;
    }

    @Transactional
    public CustomerOrder updateShippingStatus(Long id, OrderStatus status) {
        CustomerOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
        if (status != OrderStatus.SHIPPED && status != OrderStatus.DELIVERED) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Solo se permite actualizar a SHIPPED o DELIVERED");
        }
        order.updateStatus(status);
        return order;
    }

    private ShippingAddress saveAddress(AppUser user, ShippingAddressRequest request) {
        if (request == null) {
            return null;
        }
        return shippingAddressRepository.save(new ShippingAddress(
                user,
                request.recipientName(),
                request.city(),
                request.addressLine(),
                request.phone()
        ));
    }

    private String nextReference() {
        byte[] bytes = new byte[8];
        secureRandom.nextBytes(bytes);
        return "MAC-" + HexFormat.of().formatHex(bytes).toUpperCase();
    }
}
