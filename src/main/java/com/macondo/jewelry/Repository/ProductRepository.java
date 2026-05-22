package com.macondo.jewelry.Repository;


import com.macondo.jewelry.Entity.Category;
import com.macondo.jewelry.Entity.Product;
import com.macondo.jewelry.Entity.ProductStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @EntityGraph(attributePaths = {"category", "materials"})
    List<Product> findByStatus(ProductStatus status);

    List<Product> findByStatusAndReservedUntilBefore(ProductStatus status, Instant now);

    @EntityGraph(attributePaths = {"category", "materials"})
    Optional<Product> findWithCategoryAndMaterialsById(Long id);

    @EntityGraph(attributePaths = {"category", "materials"})
    List<Product> findAllByOrderByIdAsc();
}
