package com.macondo.jewelry.product;

import static com.macondo.jewelry.product.ProductDtos.ProductResponse;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponse> all() {
        return productService.availableProducts().stream().map(ProductResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ProductResponse find(@PathVariable Long id) {
        return ProductResponse.from(productService.find(id));
    }
}
