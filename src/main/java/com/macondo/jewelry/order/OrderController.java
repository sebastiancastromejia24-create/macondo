package com.macondo.jewelry.order;

import static com.macondo.jewelry.order.OrderDtos.CreateOrderRequest;
import static com.macondo.jewelry.order.OrderDtos.OrderResponse;

import com.macondo.jewelry.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody CreateOrderRequest request) {
        return OrderResponse.from(orderService.createPendingOrder(user.user(), request.productId()));
    }

    @GetMapping
    public List<OrderResponse> history(@AuthenticationPrincipal AuthenticatedUser user) {
        return orderService.history(user.user()).stream().map(OrderResponse::from).toList();
    }

    @GetMapping("/{reference}")
    public OrderResponse find(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String reference) {
        return OrderResponse.from(orderService.findOwned(reference, user.user()));
    }
}
