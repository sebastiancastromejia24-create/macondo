package com.macondo.jewelry.Entity;

import com.macondo.jewelry.Entity.Product;
import com.macondo.jewelry.Entity.AppUser;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_id")
    private ShippingAddress shippingAddress;

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

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<com.macondo.jewelry.Entity.PaymentTransaction> paymentTransactions = new ArrayList<>();

    protected CustomerOrder() {
    }

    public CustomerOrder(String reference, AppUser user, Product product, ShippingAddress shippingAddress, long productAmountCents, long wompiCommissionCents, long totalAmountCents) {
        this.reference = reference;
        this.user = user;
        this.product = product;
        this.shippingAddress = shippingAddress;
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

    public ShippingAddress getShippingAddress() {
        return shippingAddress;
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

    public List<com.macondo.jewelry.Entity.PaymentTransaction> getPaymentTransactions() {
        return paymentTransactions;
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
