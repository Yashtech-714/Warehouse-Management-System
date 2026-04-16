package com.wms.controller;

import com.wms.model.core.IPurchaseOrder;
import com.wms.model.core.OrderItem;
import com.wms.model.core.PurchaseOrder;
import com.wms.repository.PurchaseOrderRepository;
import com.wms.service.PurchaseOrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * PurchaseOrderController — role-restricted:
 *
 * MANAGER  : POST /purchase-orders (create), POST /purchase-orders/send/{id}
 * SUPPLIER : PATCH /purchase-orders/{id}/deliver
 * STAFF    : POST /purchase-orders/{id}/receive
 * ALL      : GET /purchase-orders
 *
 * SOLID SRP: thin controller, delegates to PurchaseOrderService.
 */
@RestController
@RequestMapping("/purchase-orders")
public class PurchaseOrderController {

	private final PurchaseOrderService    purchaseOrderService;
	private final PurchaseOrderRepository purchaseOrderRepository;

	public PurchaseOrderController(PurchaseOrderService purchaseOrderService,
	                                PurchaseOrderRepository purchaseOrderRepository) {
		this.purchaseOrderService    = purchaseOrderService;
		this.purchaseOrderRepository = purchaseOrderRepository;
	}

	/** All roles can view purchase orders. */
	@GetMapping
	public ResponseEntity<List<PurchaseOrder>> getAllPurchaseOrders() {
		return ResponseEntity.ok(purchaseOrderRepository.findAll());
	}

	/** MANAGER creates a PO. */
	@PostMapping
	public ResponseEntity<?> createPurchaseOrder(
			@RequestHeader(value = "X-User-Role", defaultValue = "") String role,
			@Valid @RequestBody CreatePurchaseOrderRequest request) {
		if (!"MANAGER".equalsIgnoreCase(role)) {
			return forbidden("Only MANAGER can create purchase orders.");
		}
		IPurchaseOrder createdPO = purchaseOrderService.createPO(request.getSupplierId(), request.getItems());
		return ResponseEntity.status(HttpStatus.CREATED).body(createdPO);
	}

	/** MANAGER sends PO to supplier → status SENT_TO_SUPPLIER. */
	@PostMapping("/send/{id}")
	public ResponseEntity<?> sendToSupplier(
			@RequestHeader(value = "X-User-Role", defaultValue = "") String role,
			@PathVariable("id") Long purchaseOrderId) {
		if (!"MANAGER".equalsIgnoreCase(role)) {
			return forbidden("Only MANAGER can send purchase orders to suppliers.");
		}
		return ResponseEntity.ok(purchaseOrderService.sendToSupplier(purchaseOrderId));
	}

	/** SUPPLIER marks PO as DELIVERED. */
	@PatchMapping("/{id}/deliver")
	public ResponseEntity<?> deliverPO(
			@RequestHeader(value = "X-User-Role", defaultValue = "") String role,
			@PathVariable("id") Long purchaseOrderId) {
		if (!"SUPPLIER".equalsIgnoreCase(role)) {
			return forbidden("Only SUPPLIER can mark a purchase order as delivered.");
		}
		return ResponseEntity.ok(purchaseOrderService.deliverPO(purchaseOrderId));
	}

	/** STAFF receives stock from a DELIVERED PO into a storage location. */
	@PostMapping("/{id}/receive")
	public ResponseEntity<?> receivePO(
			@RequestHeader(value = "X-User-Role", defaultValue = "") String role,
			@PathVariable("id") Long purchaseOrderId,
			@RequestBody ReceiveRequest request) {
		if (!"STAFF".equalsIgnoreCase(role)) {
			return forbidden("Only STAFF can receive stock from a purchase order.");
		}
		return ResponseEntity.ok(purchaseOrderService.receivePO(purchaseOrderId, request.getStorageLocationId()));
	}

	// ── helpers ────────────────────────────────────────────────────────────

	private ResponseEntity<?> forbidden(String message) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(Map.of("error", "FORBIDDEN", "message", message));
	}

	// ── inner DTOs ─────────────────────────────────────────────────────────

	public static class CreatePurchaseOrderRequest {
		@NotNull(message = "Supplier id is required.")
		@Min(value = 1, message = "Supplier id must be greater than zero.")
		private Long supplierId;

		@NotEmpty(message = "Purchase order must include at least one item.")
		@Valid
		private List<OrderItem> items;

		public Long getSupplierId() { return supplierId; }
		public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
		public List<OrderItem> getItems() { return items; }
		public void setItems(List<OrderItem> items) { this.items = items; }
	}

	public static class ReceiveRequest {
		private Long storageLocationId;
		public Long getStorageLocationId() { return storageLocationId; }
		public void setStorageLocationId(Long storageLocationId) { this.storageLocationId = storageLocationId; }
	}
}
