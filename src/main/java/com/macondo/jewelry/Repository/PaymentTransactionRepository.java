package com.macondo.jewelry.Repository;


import com.macondo.jewelry.Entity.PaymentTransaction;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByWompiTransactionId(String wompiTransactionId);
}
