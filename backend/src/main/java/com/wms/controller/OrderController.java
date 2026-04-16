package com.wms.controller;

import com.wms.facade.OrderFacade;
import com.wms.model.core.IOrder;
import com.wms.model.core.Order;
import com.wms.model.core.OrderItem;
import com.wms.repository.OrderRepository;
import com.wms.service.OrderService;
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
 * OrderController — role-restricted via X-User-Role header.
 *
 * MANAGER : GET /orders only (monitoring)
 * CUSTOMER: POST /orders (create), DELETE /orders/{id} (cancel)
 * STAFF   : POST /orders/process/{id}
 *
 * SOLID SRP: only HTTP routing, delegates to OrderFacade / OrderService.
 * Facade Pattern: create and cancel route through OrderFacade.
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

	private static final String ROLE_MANAGER  = "MANAGER";
	private static final String ROLE_CUSTOMER = "CUSTOMER";
	private static final String ROLE_STAFF    = "STAFF";

	private final OrderFacade   orderFacade;
	private final OrderService  orderService;
	private final OrderRepository orderRepository;

	public OrderController(OrderFacade orderFacade,
	                        OrderService orderService,
	                        OrderRepository orderRepository) {
		this.orderFacade    = orderFacade;
		this.orderService   = orderService;
		this.orderRepository = orderRepository;
	}

	/** All roles may view orders. */
	@GetMapping
	public ResponseEntity<List<Order>> getAllOrders() {
		return ResponseEntity.ok(orderRepository.findAll());
	}

	/**
	 * MANAGER: get all orders currently parked with PENDING_STOCK status.
	 * Used in Manager Dashboard to identify orders that need a Purchase Order.
	 */
	@GetMapping("/pending-stock")
	public ResponseEntity<?> getPendingStockOrders(
			@RequestHeader(value = "X-User-Role", defaultValue = "") String role) {
		if (!ROLE_MANAGER.equalsIgnoreCase(role)) {
			return forbidden("Only MANAGER can view pending-stock orders.");
		}
		return ResponseEntity.ok(orderRepository.findByStatus("PENDING_STOCK"));
	}

	/**
	 * Only CUSTOMER can create an order.
	 * Backend enforces role even if frontend is bypassed.
	 */
	@PostMapping
	public ResponseEntity<?> createOrder(
			@RequestHeader(value = "X-User-Role", defaultValue = "") String role,
			@Valid @RequestBody CreateOrderRequest request) {

		if (!ROLE_CUSTOMER.equalsIgnoreCase(role)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("error", "FORBIDDEN",
							"message", "Only CUSTOMER can create orders."));
		}
		IOrder createdOrder = orderFacade.placeOrder(request.getCustomerId(), request.getItems());
		return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
	}

	/**
	 * Only STAFF can process an order.
	 */
	@PostMapping("/process/{id}")
	public ResponseEntity<?> processOrder(
			@RequestHeader(value = "X-User-Role", defaultValue = "") String role,
			@PathVariable("id") Long orderId) {

		if (!ROLE_STAFF.equalsIgnoreCase(role)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("error", "FORBIDDEN",
							"message", "Only STAFF can process orders."));
		}
		IOrder processedOrder = orderService.processOrder(orderId);
		return ResponseEntity.ok(processedOrder);
	}

	/**
	 * Only CUSTOMER can cancel their order.
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<?> cancelOrder(
			@RequestHeader(value = "X-User-Role", defaultValue = "") String role,
			@PathVariable("id") Long orderId) {

		if (!ROLE_CUSTOMER.equalsIgnoreCase(role)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("error", "FORBIDDEN",
							"message", "Only CUSTOMER can cancel orders."));
		}
		IOrder cancelledOrder = orderFacade.cancelOrder(orderId);
		return ResponseEntity.ok(cancelledOrder);
	}

	// ── helpers ──────────────────────────────────────────────────────────────

	private ResponseEntity<?> forbidden(String message) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(Map.of("error", "FORBIDDEN", "message", message));
	}

	// ── inner DTO ──────────────────────────────────────────────────────────

	public static class CreateOrderRequest {
		@NotNull(message = "Customer id is required.")
		@Min(value = 1, message = "Customer id must be greater than zero.")
		private Long customerId;

		@NotEmpty(message = "Order must include at least one item.")
		@Valid
		private List<OrderItem> items;

		public Long getCustomerId() { return customerId; }
		public void setCustomerId(Long customerId) { this.customerId = customerId; }
		public List<OrderItem> getItems() { return items; }
		public void setItems(List<OrderItem> items) { this.items = items; }
	}
}
