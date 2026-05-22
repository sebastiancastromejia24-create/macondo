package com.macondo.jewelry.Controller;


import com.macondo.jewelry.Controller.Dtos.OrderDtos;
import com.macondo.jewelry.Entity.OrderStatus;
import com.macondo.jewelry.Mapper.OrderMapper;
import com.macondo.jewelry.Service.OrderService;
import com.macondo.jewelry.Controller.Dtos.OrderDtos.OrderResponse;
import com.macondo.jewelry.Controller.Dtos.OrderDtos.UpdateOrderStatusRequest;
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
@RequestMapping("/api/v1/admin/pedidos")
public class AdminOrderController {
    private final OrderService orderService;
    private final OrderMapper orderMapper;

    public AdminOrderController(OrderService orderService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    @GetMapping
    public List<OrderResponse> all(@RequestParam(required = false) OrderStatus status) {
        return orderService.adminOrders(status).stream().map(orderMapper::toResponse).toList();
    }

    @PatchMapping("/{id}")
    public OrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderMapper.toResponse(orderService.updateShippingStatus(id, request.status()));
    }
}
