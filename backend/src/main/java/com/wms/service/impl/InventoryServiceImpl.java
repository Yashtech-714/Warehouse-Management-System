package com.wms.service.impl;

import com.wms.model.core.InventoryItem;
import com.wms.model.core.Product;
import com.wms.model.core.StorageLocation;
import com.wms.repository.InventoryRepository;
import com.wms.repository.ProductRepository;
import com.wms.repository.StorageLocationRepository;
import com.wms.service.InventoryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * InventoryServiceImpl — manages stock levels.
 * SOLID SRP: only inventory logic here.
 * GRASP Information Expert: owns all inventory mutation.
 */
@Service
public class InventoryServiceImpl implements InventoryService {

	private final InventoryRepository inventoryRepository;
	private final ProductRepository productRepository;
	private final StorageLocationRepository storageLocationRepository;

	public InventoryServiceImpl(InventoryRepository inventoryRepository,
	                             ProductRepository productRepository,
	                             StorageLocationRepository storageLocationRepository) {
		this.inventoryRepository = inventoryRepository;
		this.productRepository = productRepository;
		this.storageLocationRepository = storageLocationRepository;
	}

	@Override
	public InventoryItem addStock(Long productId, Long storageLocationId, Integer quantity) {
		validatePositiveQuantity(quantity);
		InventoryItem item = findInventoryItem(productId, storageLocationId);
		if (item == null) {
			item = new InventoryItem();
			Product product = productRepository.findById(productId)
					.orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
			StorageLocation location = resolveStorageLocation(storageLocationId);
			item.setProduct(product);
			item.setStorageLocation(location);
			item.setQuantity(0);
			item.setReorderLevel(10);
		}
		item.setQuantity(safeQty(item.getQuantity()) + quantity);
		return inventoryRepository.save(item);
	}

	@Override
	public InventoryItem removeStock(Long productId, Long storageLocationId, Integer quantity) {
		validatePositiveQuantity(quantity);
		InventoryItem item = findInventoryItem(productId, storageLocationId);
		if (item == null) {
			throw new IllegalArgumentException("Inventory item not found for product: " + productId);
		}
		int current = safeQty(item.getQuantity());
		if (current < quantity) {
			throw new IllegalStateException("Insufficient stock for product: " + productId);
		}
		item.setQuantity(current - quantity);
		return inventoryRepository.save(item);
	}

	@Override
	public boolean checkAvailability(Long productId, Integer requestedQuantity) {
		validatePositiveQuantity(requestedQuantity);
		int total = inventoryRepository.findByProduct_Id(productId)
				.stream()
				.mapToInt(i -> safeQty(i.getQuantity()))
				.sum();
		return total >= requestedQuantity;
	}

	/**
	 * Deduct stock for an order by consuming from locations with available stock.
	 * GRASP Information Expert: this class knows where stock is stored.
	 */
	@Override
	public void deductForOrder(Long productId, Integer quantity) {
		validatePositiveQuantity(quantity);
		List<InventoryItem> items = inventoryRepository.findByProduct_Id(productId)
				.stream()
				.filter(i -> safeQty(i.getQuantity()) > 0)
				.toList();

		int remaining = quantity;
		for (InventoryItem item : items) {
			if (remaining <= 0) break;
			int available = safeQty(item.getQuantity());
			int toDeduct = Math.min(available, remaining);
			item.setQuantity(available - toDeduct);
			inventoryRepository.save(item);
			remaining -= toDeduct;
		}

		if (remaining > 0) {
			throw new IllegalStateException("Insufficient stock to fulfill order for product: " + productId);
		}
	}

	/** Staff assigns an inventory item to a specific storage location. */
	@Override
	public InventoryItem assignToLocation(Long inventoryItemId, Long storageLocationId) {
		InventoryItem item = inventoryRepository.findById(inventoryItemId)
				.orElseThrow(() -> new IllegalArgumentException("Inventory item not found: " + inventoryItemId));
		StorageLocation location = storageLocationRepository.findById(storageLocationId)
				.orElseThrow(() -> new IllegalArgumentException("Storage location not found: " + storageLocationId));
		item.setStorageLocation(location);
		return inventoryRepository.save(item);
	}

	/** Staff moves inventory item to a different storage location. */
	@Override
	public InventoryItem moveBetweenLocations(Long inventoryItemId, Long targetLocationId) {
		return assignToLocation(inventoryItemId, targetLocationId); // same operation
	}

	@Override
	public List<InventoryItem> getAllInventoryItems() {
		return inventoryRepository.findAll();
	}

	// ── private helpers ─────────────────────────────────────────────────────

	/**
	 * Finds an existing inventory item for (product, location).
	 * Uses a direct JPA query instead of in-memory filtering to guarantee
	 * no duplicate rows are created on repeated stock receipts.
	 */
	private InventoryItem findInventoryItem(Long productId, Long storageLocationId) {
		return inventoryRepository
				.findByProduct_IdAndStorageLocation_Id(productId, storageLocationId)
				.orElse(null);
	}

	private StorageLocation resolveStorageLocation(Long id) {
		return storageLocationRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Storage location not found: " + id));
	}

	private static void validatePositiveQuantity(Integer q) {
		if (q == null || q <= 0) throw new IllegalArgumentException("Quantity must be greater than zero.");
	}

	private static int safeQty(Integer q) {
		return q == null ? 0 : q;
	}
}
