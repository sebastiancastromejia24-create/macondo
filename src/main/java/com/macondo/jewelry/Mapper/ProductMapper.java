package com.macondo.jewelry.Mapper;

import com.macondo.jewelry.Controller.dto.response.ProductResponse;
import com.macondo.jewelry.Entity.Product;
import com.macondo.jewelry.Entity.Material;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "categoryId", expression = "java(product.getCategory() == null ? null : product.getCategory().getId())")
    @Mapping(target = "categoryName", expression = "java(product.getCategory() == null ? null : product.getCategory().getName())")
    @Mapping(target = "materialNames", expression = "java(materialNames(product.getMaterials()))")
    ProductResponse toResponse(Product product);

    default Set<String> materialNames(Set<Material> materials) {
        return materials.stream().map(Material::getName).collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }
}
