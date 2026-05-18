package com.macondo.jewelry.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    boolean existsByEmail(String email);

    Optional<AppUser> findByEmail(String email);
}
