package com.macondo.jewelry.Controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ShippingAddressRequest(
        @NotBlank String recipientName,
        @NotBlank String city,
        @NotBlank String addressLine,
        @NotBlank String phone
) {
}
