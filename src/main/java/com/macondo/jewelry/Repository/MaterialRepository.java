package com.macondo.jewelry.Repository;


import com.macondo.jewelry.Entity.Material;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    Optional<Material> findByNameIgnoreCase(String name);
}
