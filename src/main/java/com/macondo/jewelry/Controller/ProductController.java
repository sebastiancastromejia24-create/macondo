package com.macondo.jewelry.Controller;


import com.macondo.jewelry.Controller.Dtos.ProductDtos;
import com.macondo.jewelry.Mapper.ProductMapper;
import com.macondo.jewelry.Service.ProductService;
import com.macondo.jewelry.Controller.Dtos.ProductDtos.ProductResponse;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductController {
    private final ProductService productService;
    private final ProductMapper productMapper;

    public ProductController(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    @GetMapping
    public List<ProductResponse> all() {
        return productService.availableProducts().stream().map(productMapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ProductResponse find(@PathVariable Long id) {
        return productMapper.toResponse(productService.findForCatalog(id));
    }
}
