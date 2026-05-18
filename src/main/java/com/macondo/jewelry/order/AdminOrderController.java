package com.macondo.jewelry.order;

import com.macondo.jewelry.order.OrderDtos.OrderResponse;
import com.macondo.jewelry.order.OrderDtos.UpdateOrderStatusRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/pedidos")
public class AdminOrderController {
    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderResponse> all(@RequestParam(required = false) OrderStatus status) {
        return orderService.adminOrders(status).stream().map(OrderResponse::from).toList();
    }

    @PatchMapping("/{id}")
    public OrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return OrderResponse.from(orderService.updateShippingStatus(id, request.status()));
    }
}
