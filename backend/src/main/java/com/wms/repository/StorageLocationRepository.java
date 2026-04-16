package com.wms.repository;

import com.wms.model.core.StorageLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StorageLocationRepository extends JpaRepository<StorageLocation, Long> {
	Optional<StorageLocation> findByCode(String code);
}
