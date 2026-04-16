package com.wms.repository;

import com.wms.model.core.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {

	/** All inventory rows for a given product (used for deductForOrder). */
	List<InventoryItem> findByProduct_Id(Long productId);

	/**
	 * Exact lookup for (product, location) pair — used to prevent duplicate rows.
	 * Returns existing inventory if it already exists, or empty if not.
	 */
	Optional<InventoryItem> findByProduct_IdAndStorageLocation_Id(Long productId, Long storageLocationId);
}
