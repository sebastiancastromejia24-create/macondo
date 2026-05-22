package com.macondo.jewelry.Entity;

import com.macondo.jewelry.Entity.CustomerOrder;
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
@Table(name = "payment_transactions")
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String wompiTransactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private long amountInCents;

    @Column(nullable = false)
    private Instant receivedAt = Instant.now();

    protected PaymentTransaction() {
    }

    public PaymentTransaction(String wompiTransactionId, CustomerOrder order, PaymentStatus status, long amountInCents) {
        this.wompiTransactionId = wompiTransactionId;
        this.order = order;
        this.status = status;
        this.amountInCents = amountInCents;
    }

    public Long getId() {
        return id;
    }

    public String getWompiTransactionId() {
        return wompiTransactionId;
    }

    public CustomerOrder getOrder() {
        return order;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public long getAmountInCents() {
        return amountInCents;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }
}
