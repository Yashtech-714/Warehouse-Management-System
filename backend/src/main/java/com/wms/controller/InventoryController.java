package com.wms.controller;

import com.wms.model.core.InventoryItem;
import com.wms.service.InventoryService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * InventoryController — role-restricted:
 *   GET  /inventory        : all roles (read-only)
 *   GET  /inventory/check  : all roles
 *   POST /inventory/assign : STAFF only
 *   POST /inventory/move   : STAFF only
 *
 * Manual stock add/remove removed — inventory only changes via PO receive or order processing.
 * SOLID SRP: only HTTP routing here.
 */
@Validated
@RestController
@RequestMapping("/inventory")
public class InventoryController {

	private final InventoryService inventoryService;

	public InventoryController(InventoryService inventoryService) {
		this.inventoryService = inventoryService;
	}

	/** All roles can view current stock. */
	@GetMapping
	public ResponseEntity<List<InventoryItem>> getAllInventory() {
		return ResponseEntity.ok(inventoryService.getAllInventoryItems());
	}

	/** All roles can check availability. */
	@GetMapping("/check")
	public ResponseEntity<Boolean> checkAvailability(
			@RequestParam @NotNull @Min(1) Long productId,
			@RequestParam @NotNull @Min(1) Integer requestedQuantity) {
		return ResponseEntity.ok(inventoryService.checkAvailability(productId, requestedQuantity));
	}

	/** STAFF assigns an inventory item to a storage location. */
	@PostMapping("/assign")
	public ResponseEntity<?> assignToLocation(
			@RequestHeader(value = "X-User-Role", defaultValue = "") String role,
			@RequestBody AssignRequest request) {
		if (!"STAFF".equalsIgnoreCase(role)) {
			return forbidden("Only STAFF can assign inventory to storage locations.");
		}
		InventoryItem updated = inventoryService.assignToLocation(
				request.getInventoryItemId(), request.getStorageLocationId());
		return ResponseEntity.ok(updated);
	}

	/** STAFF moves an inventory item between storage locations. */
	@PostMapping("/move")
	public ResponseEntity<?> moveBetweenLocations(
			@RequestHeader(value = "X-User-Role", defaultValue = "") String role,
			@RequestBody AssignRequest request) {
		if (!"STAFF".equalsIgnoreCase(role)) {
			return forbidden("Only STAFF can move inventory between locations.");
		}
		InventoryItem updated = inventoryService.moveBetweenLocations(
				request.getInventoryItemId(), request.getStorageLocationId());
		return ResponseEntity.ok(updated);
	}

	// ── helpers ────────────────────────────────────────────────────────────

	private ResponseEntity<?> forbidden(String message) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(Map.of("error", "FORBIDDEN", "message", message));
	}

	// ── inner DTO ──────────────────────────────────────────────────────────

	public static class AssignRequest {
		private Long inventoryItemId;
		private Long storageLocationId;

		public Long getInventoryItemId() { return inventoryItemId; }
		public void setInventoryItemId(Long inventoryItemId) { this.inventoryItemId = inventoryItemId; }
		public Long getStorageLocationId() { return storageLocationId; }
		public void setStorageLocationId(Long storageLocationId) { this.storageLocationId = storageLocationId; }
	}
}
