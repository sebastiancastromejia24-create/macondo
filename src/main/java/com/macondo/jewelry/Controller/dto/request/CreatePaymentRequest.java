package com.macondo.jewelry.Controller.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CreatePaymentRequest(@NotNull Long productId, @Valid ShippingAddressRequest shippingAddress) {
}
