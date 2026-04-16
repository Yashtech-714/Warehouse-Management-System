package com.wms.controller;

import com.wms.model.core.Product;
import com.wms.repository.ProductRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * ProductController — MANAGER manages products; all roles can read.
 * SOLID SRP: thin controller, delegates to repository.
 * Role guard: mutations blocked for non-MANAGER via X-User-Role header.
 */
@RestController
@RequestMapping("/products")
public class ProductController {

	private final ProductRepository productRepository;

	public ProductController(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	/** All roles can view active products. */
	@GetMapping
	public ResponseEntity<List<Product>> getAllProducts() {
		return ResponseEntity.ok(productRepository.findByActiveTrue());
	}

	/** MANAGER creates a product. */
	@PostMapping
	public ResponseEntity<?> createProduct(
			@RequestHeader(value = "X-User-Role", defaultValue = "") String role,
			@Valid @RequestBody ProductRequest request) {
		if (!"MANAGER".equalsIgnoreCase(role)) {
			return forbidden("Only MANAGER can create products.");
		}
		Product product = new Product();
		applyRequest(product, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(productRepository.save(product));
	}

	/** MANAGER updates a product. */
	@PutMapping("/{id}")
	public ResponseEntity<?> updateProduct(
			@RequestHeader(value = "X-User-Role", defaultValue = "") String role,
			@PathVariable Long id,
			@Valid @RequestBody ProductRequest request) {
		if (!"MANAGER".equalsIgnoreCase(role)) {
			return forbidden("Only MANAGER can update products.");
		}
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
		applyRequest(product, request);
		return ResponseEntity.ok(productRepository.save(product));
	}

	/** MANAGER soft-deletes a product. */
	@PatchMapping("/{id}/deactivate")
	public ResponseEntity<?> deactivateProduct(
			@RequestHeader(value = "X-User-Role", defaultValue = "") String role,
			@PathVariable Long id) {
		if (!"MANAGER".equalsIgnoreCase(role)) {
			return forbidden("Only MANAGER can deactivate products.");
		}
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
		product.setActive(false);
		return ResponseEntity.ok(productRepository.save(product));
	}

	// ── helpers ──────────────────────────────────────────────────────────────

	private void applyRequest(Product product, ProductRequest req) {
		product.setSku(req.getSku().trim());
		product.setName(req.getName().trim());
		product.setDescription(req.getDescription());
		product.setUnitPrice(req.getUnitPrice());
		product.setCategory(req.getCategory());
	}

	private ResponseEntity<?> forbidden(String message) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(Map.of("error", "FORBIDDEN", "message", message));
	}

	// ── inner DTO ─────────────────────────────────────────────────────────────

	public static class ProductRequest {
		@NotBlank(message = "SKU is required.")
		private String sku;
		@NotBlank(message = "Product name is required.")
		private String name;
		private String description;
		@NotNull(message = "Unit price is required.")
		@DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than zero.")
		private BigDecimal unitPrice;
		private String category;

		public String getSku() { return sku; }
		public void setSku(String sku) { this.sku = sku; }
		public String getName() { return name; }
		public void setName(String name) { this.name = name; }
		public String getDescription() { return description; }
		public void setDescription(String description) { this.description = description; }
		public BigDecimal getUnitPrice() { return unitPrice; }
		public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
		public String getCategory() { return category; }
		public void setCategory(String category) { this.category = category; }
	}
}
