package com.wms.controller;

import com.wms.model.user.Supplier;
import com.wms.repository.SupplierRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * SupplierController — read-only supplier lookup.
 * SOLID SRP: only supplier data access here.
 * Used by Manager to populate the Supplier dropdown in PO creation.
 */
@RestController
@RequestMapping("/suppliers")
public class SupplierController {

	private final SupplierRepository supplierRepository;

	public SupplierController(SupplierRepository supplierRepository) {
		this.supplierRepository = supplierRepository;
	}

	/** GET /suppliers — all roles can fetch supplier list (needed for PO dropdown). */
	@GetMapping
	public ResponseEntity<List<Supplier>> getAllSuppliers() {
		return ResponseEntity.ok(supplierRepository.findAll());
	}
}
