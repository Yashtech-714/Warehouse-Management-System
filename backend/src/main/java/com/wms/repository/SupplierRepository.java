package com.wms.repository;

import com.wms.model.user.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findByEmail(String email);
}

