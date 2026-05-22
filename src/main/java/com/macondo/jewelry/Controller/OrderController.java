package com.macondo.jewelry.Controller;


import com.macondo.jewelry.Controller.Dtos.OrderDtos;
import com.macondo.jewelry.Entity.ShippingAddress;
import com.macondo.jewelry.Mapper.OrderMapper;
import com.macondo.jewelry.Service.OrderService;
import com.macondo.jewelry.Controller.Dtos.OrderDtos.CreateOrderRequest;
import com.macondo.jewelry.Controller.Dtos.OrderDtos.OrderResponse;
import com.macondo.jewelry.Security.AuthenticatedUser;
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
@RequestMapping("/api/v1/pedidos")
public class OrderController {
    private final OrderService orderService;
    private final OrderMapper orderMapper;

    public OrderController(OrderService orderService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody CreateOrderRequest request) {
        return orderMapper.toResponse(orderService.createPendingOrder(user.user(), request.productId(), request.shippingAddress()));
    }

    @GetMapping
    public List<OrderResponse> history(@AuthenticationPrincipal AuthenticatedUser user) {
        return orderService.history(user.user()).stream().map(orderMapper::toResponse).toList();
    }

    @GetMapping("/{reference}")
    public OrderResponse find(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String reference) {
        return orderMapper.toResponse(orderService.findOwned(reference, user.user()));
    }
}
