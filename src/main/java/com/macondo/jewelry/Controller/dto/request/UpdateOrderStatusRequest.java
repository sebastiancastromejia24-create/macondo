package com.macondo.jewelry.Controller.dto.request;

import com.macondo.jewelry.Entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(@NotNull OrderStatus status) {
}
