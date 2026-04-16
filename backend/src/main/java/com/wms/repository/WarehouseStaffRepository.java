package com.wms.repository;

import com.wms.model.user.WarehouseStaff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WarehouseStaffRepository extends JpaRepository<WarehouseStaff, Long> {
    Optional<WarehouseStaff> findByEmail(String email);
}
