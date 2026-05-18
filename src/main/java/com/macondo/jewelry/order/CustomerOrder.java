package com.macondo.jewelry.order;

import com.macondo.jewelry.product.Product;
import com.macondo.jewelry.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "orders")
public class CustomerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private long productAmountCents;

    @Column(nullable = false)
    private long wompiCommissionCents;

    @Column(nullable = false)
    private long totalAmountCents;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    @Column(nullable = false)
    private boolean webhookProcessed;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt;

    protected CustomerOrder() {
    }

    public CustomerOrder(String reference, AppUser user, Product product, long productAmountCents, long wompiCommissionCents, long totalAmountCents) {
        this.reference = reference;
        this.user = user;
        this.product = product;
        this.productAmountCents = productAmountCents;
        this.wompiCommissionCents = wompiCommissionCents;
        this.totalAmountCents = totalAmountCents;
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public AppUser getUser() {
        return user;
    }

    public Product getProduct() {
        return product;
    }

    public long getProductAmountCents() {
        return productAmountCents;
    }

    public long getWompiCommissionCents() {
        return wompiCommissionCents;
    }

    public long getTotalAmountCents() {
        return totalAmountCents;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public boolean isWebhookProcessed() {
        return webhookProcessed;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void updateStatus(OrderStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public void markWebhookProcessed() {
        this.webhookProcessed = true;
        this.updatedAt = Instant.now();
    }
}
