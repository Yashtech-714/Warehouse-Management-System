package com.wms.service;

import com.wms.model.core.InventoryItem;

import java.util.List;

/**
 * InventoryService — manages warehouse stock.
 * SOLID OCP: inventory changes only via defined business operations (not arbitrary add/remove).
 */
public interface InventoryService {

	/** Internal: add quantity to an inventory item (used by PO receiving). */
	InventoryItem addStock(Long productId, Long storageLocationId, Integer quantity);

	/** Internal: deduct quantity from inventory (used by order processing). */
	InventoryItem removeStock(Long productId, Long storageLocationId, Integer quantity);

	/** Check if enough total stock exists across all locations for a product. */
	boolean checkAvailability(Long productId, Integer requestedQuantity);

	/** Deduct stock for all items of a processed order (picks from any available location). */
	void deductForOrder(Long productId, Integer quantity);

	/** Assign an existing inventory item to a storage location (Staff operation). */
	InventoryItem assignToLocation(Long inventoryItemId, Long storageLocationId);

	/** Move inventory item to a different storage location (Staff operation). */
	InventoryItem moveBetweenLocations(Long inventoryItemId, Long targetLocationId);

	List<InventoryItem> getAllInventoryItems();
}
