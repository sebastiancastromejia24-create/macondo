package com.macondo.jewelry.Config;

import com.macondo.jewelry.Entity.Category;
import com.macondo.jewelry.Repository.CategoryRepository;
import com.macondo.jewelry.Entity.Material;
import com.macondo.jewelry.Repository.MaterialRepository;
import com.macondo.jewelry.Entity.Product;
import com.macondo.jewelry.Repository.ProductRepository;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CatalogBootstrap implements CommandLineRunner {
    private final CategoryRepository categoryRepository;
    private final MaterialRepository materialRepository;
    private final ProductRepository productRepository;

    public CatalogBootstrap(CategoryRepository categoryRepository, MaterialRepository materialRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.materialRepository = materialRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return;
        }
        Category rings = categoryRepository.save(new Category("Anillos", "Anillos tejidos ajustables elaborados a mano."));
        Category bracelets = categoryRepository.save(new Category("Manillas", "Manillas artesanales en balineria y oro laminado."));
        Material goldLaminated = materialRepository.save(new Material("Oro laminado 18k", "Acabado brillante de uso diario."));
        Material goldBalinery = materialRepository.save(new Material("Balineria oro 18k", "Balineria artesanal de alta duracion."));

        productRepository.save(new Product(
                "Anillo Macondo Sol",
                "Anillo tejido ajustable con acabado dorado y textura artesanal.",
                14500000,
                "/images/11.webp",
                rings,
                Set.of(goldLaminated)
        ));
        productRepository.save(new Product(
                "Manilla Rio Magdalena",
                "Manilla tejida inspirada en formas organicas colombianas.",
                18000000,
                "/images/14.webp",
                bracelets,
                Set.of(goldBalinery, goldLaminated)
        ));
        productRepository.save(new Product(
                "Anillo Palma",
                "Pieza ajustable para uso diario, hecha bajo pedido corto.",
                16000000,
                "/images/15.webp",
                rings,
                Set.of(goldBalinery)
        ));
    }
}
