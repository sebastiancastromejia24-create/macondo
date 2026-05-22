package com.macondo.jewelry.Mapper;

import com.macondo.jewelry.Controller.dto.response.OrderResponse;
import com.macondo.jewelry.Entity.CustomerOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "shippingCity", expression = "java(order.getShippingAddress() == null ? null : order.getShippingAddress().getCity())")
    @Mapping(target = "shippingAddressLine", expression = "java(order.getShippingAddress() == null ? null : order.getShippingAddress().getAddressLine())")
    OrderResponse toResponse(CustomerOrder order);
}
