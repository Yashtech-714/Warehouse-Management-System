package com.wms.repository;

import com.wms.model.user.WarehouseManager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WarehouseManagerRepository extends JpaRepository<WarehouseManager, Long> {
    Optional<WarehouseManager> findByEmail(String email);
}
