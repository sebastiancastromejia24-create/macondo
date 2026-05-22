package com.macondo.jewelry.Repository;


import com.macondo.jewelry.Entity.ShippingAddress;
import com.macondo.jewelry.Entity.AppUser;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Long> {
    List<ShippingAddress> findByUserOrderByIdDesc(AppUser user);
}
