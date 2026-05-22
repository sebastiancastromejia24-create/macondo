package com.macondo.jewelry.Service;

import com.macondo.jewelry.Controller.dto.request.ProductRequest;
import com.macondo.jewelry.Entity.Product;
import com.macondo.jewelry.Entity.ProductStatus;
import com.macondo.jewelry.Repository.ProductRepository;
import com.macondo.jewelry.Entity.Category;
import com.macondo.jewelry.Repository.CategoryRepository;
import com.macondo.jewelry.Entity.Material;
import com.macondo.jewelry.Repository.MaterialRepository;
import com.macondo.jewelry.Common.BusinessException;
import com.macondo.jewelry.Common.ResourceNotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final MaterialRepository materialRepository;
    private final Clock clock;
    private final long reservationTtlMinutes;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            MaterialRepository materialRepository,
            @Value("${app.reservations.ttl-minutes}") long reservationTtlMinutes
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.materialRepository = materialRepository;
        this.reservationTtlMinutes = reservationTtlMinutes;
        this.clock = Clock.systemUTC();
    }

    @Transactional(readOnly = true)
    public List<Product> allProducts() {
        return productRepository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public List<Product> availableProducts() {
        return productRepository.findByStatus(ProductStatus.AVAILABLE);
    }

    @Transactional(readOnly = true)
    public Product find(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
    }

    @Transactional(readOnly = true)
    public Product findForCatalog(Long id) {
        return productRepository.findWithCategoryAndMaterialsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
    }

    @Transactional
    public Product create(ProductRequest request) {
        return productRepository.save(new Product(
                request.name(),
                request.description(),
                request.priceCents(),
                request.imageUrl(),
                findCategory(request.categoryId()),
                findMaterials(request.materialIds())
        ));
    }

    @Transactional
    public Product update(Long id, ProductRequest request) {
        Product product = find(id);
        product.update(
                request.name(),
                request.description(),
                request.priceCents(),
                request.imageUrl(),
                findCategory(request.categoryId()),
                findMaterials(request.materialIds()),
                request.status() == null ? ProductStatus.AVAILABLE : request.status()
        );
        return product;
    }

    @Transactional
    public Product deactivate(Long id) {
        Product product = find(id);
        product.update(product.getName(), product.getDescription(), product.getPriceCents(), product.getImageUrl(), product.getCategory(), product.getMaterials(), ProductStatus.INACTIVE);
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
            throw new BusinessException(HttpStatus.CONFLICT, "Producto no disponible temporalmente");
        }
        product.reserveUntil(now.plusSeconds(reservationTtlMinutes * 60));
        return productRepository.saveAndFlush(product);
    }

    private Category findCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada"));
    }

    private Set<Material> findMaterials(Set<Long> materialIds) {
        if (materialIds == null || materialIds.isEmpty()) {
            return Set.of();
        }
        Set<Material> materials = new LinkedHashSet<>(materialRepository.findAllById(materialIds));
        if (materials.size() != materialIds.size()) {
            throw new ResourceNotFoundException("Uno o mas materiales no existen");
        }
        return materials;
    }
}
