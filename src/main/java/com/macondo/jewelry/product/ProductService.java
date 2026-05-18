package com.macondo.jewelry.product;

import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final Clock clock;
    private final long reservationTtlMinutes;

    public ProductService(ProductRepository productRepository, @Value("${app.reservations.ttl-minutes}") long reservationTtlMinutes) {
        this.productRepository = productRepository;
        this.reservationTtlMinutes = reservationTtlMinutes;
        this.clock = Clock.systemUTC();
    }

    @Transactional(readOnly = true)
    public List<Product> availableProducts() {
        return productRepository.findByStatus(ProductStatus.AVAILABLE);
    }

    @Transactional(readOnly = true)
    public Product find(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
    }

    @Transactional
    public Product create(ProductDtos.ProductRequest request) {
        return productRepository.save(new Product(
                request.name(),
                request.description(),
                request.priceCents(),
                request.imageUrl()
        ));
    }

    @Transactional
    public Product update(Long id, ProductDtos.ProductRequest request) {
        Product product = find(id);
        product.update(
                request.name(),
                request.description(),
                request.priceCents(),
                request.imageUrl(),
                request.status() == null ? ProductStatus.AVAILABLE : request.status()
        );
        return product;
    }

    @Transactional
    public Product deactivate(Long id) {
        Product product = find(id);
        product.update(product.getName(), product.getDescription(), product.getPriceCents(), product.getImageUrl(), ProductStatus.INACTIVE);
        return product;
    }

    @Transactional
    public void delete(Long id) {
        Product product = find(id);
        productRepository.delete(product);
    }

    @Transactional
    public Product reserve(Long productId) {
        Product product = find(productId);
        Instant now = Instant.now(clock);
        if (!product.isAvailableForCheckout(now)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Producto no disponible temporalmente");
        }
        product.reserveUntil(now.plusSeconds(reservationTtlMinutes * 60));
        return productRepository.saveAndFlush(product);
    }
}
