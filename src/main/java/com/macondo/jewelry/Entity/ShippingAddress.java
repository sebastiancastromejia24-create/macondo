package com.macondo.jewelry.Entity;

import com.macondo.jewelry.Entity.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "shipping_addresses")
public class ShippingAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String addressLine;

    @Column(nullable = false)
    private String phone;

    protected ShippingAddress() {
    }

    public ShippingAddress(AppUser user, String recipientName, String city, String addressLine, String phone) {
        this.user = user;
        this.recipientName = recipientName;
        this.city = city;
        this.addressLine = addressLine;
        this.phone = phone;
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getCity() {
        return city;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public String getPhone() {
        return phone;
    }
}
