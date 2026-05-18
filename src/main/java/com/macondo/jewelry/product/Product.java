package com.macondo.jewelry.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.time.Instant;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.AVAILABLE;

    private Instant reservedUntil;

    @Version
    private long version;

    protected Product() {
    }

    public Product(String name, String description, long priceCents, String imageUrl) {
        this.name = name;
        this.description = description;
        this.priceCents = priceCents;
        this.imageUrl = imageUrl;
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

    public void update(String name, String description, long priceCents, String imageUrl, ProductStatus status) {
        this.name = name;
        this.description = description;
        this.priceCents = priceCents;
        this.imageUrl = imageUrl;
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
