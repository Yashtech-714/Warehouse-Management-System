package com.wms.controller;

import com.wms.model.core.StorageLocation;
import com.wms.repository.StorageLocationRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * StorageLocationController
 * - GET /storage-locations         : all roles (Staff assigns/moves inventory using location IDs)
 * - POST /storage-locations        : MANAGER only
 * - PUT  /storage-locations/{id}   : MANAGER only
 *
 * SOLID SRP: location management only.
 */
@RestController
@RequestMapping("/storage-locations")
public class StorageLocationController {

	private final StorageLocationRepository repository;

	public StorageLocationController(StorageLocationRepository repository) {
		this.repository = repository;
	}

	/** All roles view locations (Staff needs them to assign inventory). */
	@GetMapping
	public ResponseEntity<List<StorageLocation>> getAll() {
		return ResponseEntity.ok(repository.findAll());
	}

	/** MANAGER creates a storage location. */
	@PostMapping
	public ResponseEntity<?> create(
			@RequestHeader(value = "X-User-Role", defaultValue = "") String role,
			@Valid @RequestBody LocationRequest request) {
		if (!"MANAGER".equalsIgnoreCase(role)) {
			return forbidden("Only MANAGER can create storage locations.");
		}
		StorageLocation loc = new StorageLocation();
		applyRequest(loc, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(loc));
	}

	/** MANAGER updates a storage location. */
	@PutMapping("/{id}")
	public ResponseEntity<?> update(
			@RequestHeader(value = "X-User-Role", defaultValue = "") String role,
			@PathVariable Long id,
			@Valid @RequestBody LocationRequest request) {
		if (!"MANAGER".equalsIgnoreCase(role)) {
			return forbidden("Only MANAGER can update storage locations.");
		}
		StorageLocation loc = repository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Storage location not found: " + id));
		applyRequest(loc, request);
		return ResponseEntity.ok(repository.save(loc));
	}

	// ── helpers ────────────────────────────────────────────────────

	private void applyRequest(StorageLocation loc, LocationRequest req) {
		loc.setCode(req.getCode().trim().toUpperCase());
		loc.setZone(req.getZone());
		loc.setAisle(req.getAisle());
		loc.setRack(req.getRack());
		loc.setBin(req.getBin());
	}

	private ResponseEntity<?> forbidden(String message) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(Map.of("error", "FORBIDDEN", "message", message));
	}

	// ── inner DTO ──────────────────────────────────────────────────

	public static class LocationRequest {
		@NotBlank(message = "Location code is required.")
		private String code;
		private String zone;
		private String aisle;
		private String rack;
		private String bin;

		public String getCode() { return code; }
		public void setCode(String code) { this.code = code; }
		public String getZone() { return zone; }
		public void setZone(String zone) { this.zone = zone; }
		public String getAisle() { return aisle; }
		public void setAisle(String aisle) { this.aisle = aisle; }
		public String getRack() { return rack; }
		public void setRack(String rack) { this.rack = rack; }
		public String getBin() { return bin; }
		public void setBin(String bin) { this.bin = bin; }
	}
}
