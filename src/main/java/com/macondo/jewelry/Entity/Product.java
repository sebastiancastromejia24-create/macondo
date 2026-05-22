package com.macondo.jewelry.Entity;

import com.macondo.jewelry.Entity.Category;
import com.macondo.jewelry.Entity.Material;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(length = 1200)
    private String description;

    @Positive
    @Column(nullable = false)
    private long priceCents;

    @Column(length = 1200)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "product_materials",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "material_id")
    )
    private Set<Material> materials = new LinkedHashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.AVAILABLE;

    private Instant reservedUntil;

    @Version
    private long version;

    protected Product() {
    }

    public Product(String name, String description, long priceCents, String imageUrl, Category category, Set<Material> materials) {
        this.name = name;
        this.description = description;
        this.priceCents = priceCents;
        this.imageUrl = imageUrl;
        this.category = category;
        this.materials = new LinkedHashSet<>(materials);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public long getPriceCents() {
        return priceCents;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Category getCategory() {
        return category;
    }

    public Set<Material> getMaterials() {
        return materials;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public Instant getReservedUntil() {
        return reservedUntil;
    }

    public long getVersion() {
        return version;
    }

    public boolean isAvailableForCheckout(Instant now) {
        return status == ProductStatus.AVAILABLE || (status == ProductStatus.RESERVED && reservedUntil != null && reservedUntil.isBefore(now));
    }

    public void update(String name, String description, long priceCents, String imageUrl, Category category, Set<Material> materials, ProductStatus status) {
        this.name = name;
        this.description = description;
        this.priceCents = priceCents;
        this.imageUrl = imageUrl;
        this.category = category;
        this.materials = new LinkedHashSet<>(materials);
        this.status = status;
        if (status != ProductStatus.RESERVED) {
            this.reservedUntil = null;
        }
    }

    public void reserveUntil(Instant reservedUntil) {
        this.status = ProductStatus.RESERVED;
        this.reservedUntil = reservedUntil;
    }

    public void release() {
        this.status = ProductStatus.AVAILABLE;
        this.reservedUntil = null;
    }

    public void markSold() {
        this.status = ProductStatus.SOLD;
        this.reservedUntil = null;
    }
}
