package com.wms.controller;

import com.wms.model.core.IShipment;
import com.wms.model.core.Shipment;
import com.wms.repository.ShipmentRepository;
import com.wms.service.ShipmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ShipmentController — role-restricted:
 *
 * GET  /shipments          : all roles (read-only)
 * GET  /shipments/{id}     : all roles (track)
 * PATCH /shipments/{id}/ship    : STAFF only → CREATED → SHIPPED
 * PATCH /shipments/{id}/deliver : STAFF only → SHIPPED → DELIVERED
 *
 * POST /shipments removed — created automatically via processOrder().
 * SOLID SRP: only HTTP routing; delegates to ShipmentService.
 */
@RestController
@RequestMapping("/shipments")
public class ShipmentController {

	private final ShipmentService    shipmentService;
	private final ShipmentRepository shipmentRepository;

	public ShipmentController(ShipmentService shipmentService,
	                           ShipmentRepository shipmentRepository) {
		this.shipmentService    = shipmentService;
		this.shipmentRepository = shipmentRepository;
	}

	/** All roles: view all shipments. */
	@GetMapping
	public ResponseEntity<List<Shipment>> getAllShipments() {
		return ResponseEntity.ok(shipmentRepository.findAll());
	}

	/** All roles: track a specific shipment. */
	@GetMapping("/{id}")
	public ResponseEntity<IShipment> trackShipment(@PathVariable("id") Long shipmentId) {
		return ResponseEntity.ok(shipmentService.trackShipment(shipmentId));
	}

	/**
	 * STAFF: mark shipment as SHIPPED (CREATED → SHIPPED).
	 * Also syncs linked order status to SHIPPED.
	 */
	@PatchMapping("/{id}/ship")
	public ResponseEntity<?> markShipped(
			@RequestHeader(value = "X-User-Role", defaultValue = "") String role,
			@PathVariable("id") Long shipmentId) {
		if (!"STAFF".equalsIgnoreCase(role)) {
			return forbidden("Only STAFF can mark shipments as shipped.");
		}
		return ResponseEntity.ok(shipmentService.markShipped(shipmentId));
	}

	/**
	 * STAFF: mark shipment as DELIVERED (SHIPPED → DELIVERED).
	 * Also syncs linked order status to DELIVERED.
	 */
	@PatchMapping("/{id}/deliver")
	public ResponseEntity<?> markDelivered(
			@RequestHeader(value = "X-User-Role", defaultValue = "") String role,
			@PathVariable("id") Long shipmentId) {
		if (!"STAFF".equalsIgnoreCase(role)) {
			return forbidden("Only STAFF can mark shipments as delivered.");
		}
		return ResponseEntity.ok(shipmentService.markDelivered(shipmentId));
	}

	// ── helpers ──────────────────────────────────────────────────────────────

	private ResponseEntity<?> forbidden(String message) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(Map.of("error", "FORBIDDEN", "message", message));
	}
}
