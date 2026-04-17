package com.wms.service.impl;

import com.wms.builder.Director;
import com.wms.builder.OrderBuilder;
import com.wms.factory.OrderFactory;
import com.wms.model.core.IOrder;
import com.wms.model.core.IShipment;
import com.wms.model.core.Order;
import com.wms.model.core.OrderItem;
import com.wms.model.core.Product;
import com.wms.model.core.enums.OrderStatus;
import com.wms.model.user.Customer;
import com.wms.observer.NotificationService;
import com.wms.observer.WmsEvent;
import com.wms.repository.CustomerRepository;
import com.wms.repository.OrderRepository;
import com.wms.repository.ProductRepository;
import com.wms.service.InventoryService;
import com.wms.service.OrderService;
import com.wms.service.ShipmentService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * OrderServiceImpl — manages order lifecycle.
 * SOLID SRP: only order business logic here.
 * GRASP Information Expert: owns order state transitions.
 * Observer: fires ORDER_PLACED on creation, notifies on cancellation.
 */
@Service
public class OrderServiceImpl implements OrderService {

	private final InventoryService inventoryService;
	private final ShipmentService shipmentService;
	private final OrderFactory orderFactory;
	private final OrderRepository orderRepository;
	private final CustomerRepository customerRepository;
	private final ProductRepository productRepository;
	private final NotificationService notificationService;

	public OrderServiceImpl(InventoryService inventoryService,
	                         ShipmentService shipmentService,
	                         OrderFactory orderFactory,
	                         OrderRepository orderRepository,
	                         CustomerRepository customerRepository,
	                         ProductRepository productRepository,
	                         NotificationService notificationService) {
		this.inventoryService = inventoryService;
		this.shipmentService = shipmentService;
		this.orderFactory = orderFactory;
		this.orderRepository = orderRepository;
		this.customerRepository = customerRepository;
		this.productRepository = productRepository;
		this.notificationService = notificationService;
	}

	/**
	 * CUSTOMER creates an order. Builder + Factory pattern.
	 *
	 * Inventory check at creation time:
	 *   - Sufficient stock  → status: CREATED  (Staff can process immediately)
	 *   - Insufficient stock → status: PENDING_STOCK (Manager notified, auto-recovers after restock)
	 *
	 * This prevents a situation where an order sits as CREATED but can never be processed.
	 */
	@Override
	public IOrder createOrder(Long customerId, List<OrderItem> items) {
		if (items == null || items.isEmpty()) {
			throw new IllegalArgumentException("Order requires at least one item.");
		}

		Customer customer = resolveCustomer(customerId);
		IOrder baseOrder = orderFactory.createOrder(customer, List.of());
		if (!(baseOrder instanceof Order seededOrder)) {
			throw new IllegalStateException("OrderFactory must return Order instance.");
		}

		OrderBuilder builder = new OrderBuilder().setCustomer(customer);
		for (OrderItem item : items) {
			Long productId = item.getProduct() == null ? null : item.getProduct().getId();
			if (productId == null) throw new IllegalArgumentException("Each order item requires a valid product id.");
			Product product = productRepository.findById(productId)
					.orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
			int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
			if (quantity <= 0) throw new IllegalArgumentException("Item quantity must be greater than zero.");
			builder.addItem(product, quantity);
		}

		Director director = new Director(builder);
		director.construct();

		Order order = builder.getResult();
		order.setOrderNumber(seededOrder.getOrderNumber());
		order.setOrderDate(seededOrder.getOrderDate());
		order.refreshTotalAmount();

		// ── Inventory check at order creation ────────────────────────────────────
		// Find the first item that cannot be fulfilled from current stock.
		String insufficientProductName = null;
		for (OrderItem item : order.getItems()) {
			Long productId = item.getProduct() == null ? null : item.getProduct().getId();
			Integer qty    = item.getQuantity();
			if (productId != null && qty != null && !inventoryService.checkAvailability(productId, qty)) {
				insufficientProductName = item.getProduct().getName() != null
						? item.getProduct().getName() : "Product #" + productId;
				break;
			}
		}

		if (insufficientProductName != null) {
			// Insufficient stock — park immediately and alert Manager
			order.setStatus(OrderStatus.PENDING_STOCK.name());
			IOrder saved = orderRepository.save(order);
			notificationService.notify(WmsEvent.LOW_STOCK,
					"PENDING_STOCK: Order #" + saved.getOrderNumber() +
					" placed by customer — insufficient stock for \"" + insufficientProductName +
					"\". Please create a Purchase Order to restock.");
			notificationService.notify(WmsEvent.ORDER_PLACED,
					"New order #" + saved.getOrderNumber() + " placed (PENDING_STOCK — awaiting restock).");
			return saved;
		}

		// Sufficient stock — ready for Staff to process
		order.setStatus(OrderStatus.CREATED.name());
		IOrder saved = orderRepository.save(order);
		notificationService.notify(WmsEvent.ORDER_PLACED,
				"New order #" + saved.getOrderNumber() + " placed and ready to process.");
		return saved;
	}

	/**
	 * STAFF processes an order:
	 * 1. Validates status is CREATED or PENDING_STOCK (retry after restock).
	 * 2. Checks inventory availability.
	 *    → If insufficient: sets status = PENDING_STOCK, fires notification (no exception thrown).
	 * 3. Deducts inventory, creates shipment, sets PROCESSED.
	 */
	@Override
	public IOrder processOrder(Long orderId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

		String currentStatus = order.getStatus();
		boolean isRetry = OrderStatus.PENDING_STOCK.name().equals(currentStatus);

		if (!OrderStatus.CREATED.name().equals(currentStatus) && !isRetry) {
			throw new IllegalStateException("Order cannot be processed. Current status: " + currentStatus);
		}

		// Check all items have enough stock
		for (OrderItem item : order.getItems()) {
			Long productId = item.getProduct() == null ? null : item.getProduct().getId();
			Integer quantity = item.getQuantity();
			if (productId == null || quantity == null) {
				throw new IllegalStateException("Invalid order item — missing product or quantity.");
			}
			if (!inventoryService.checkAvailability(productId, quantity)) {
				// Insufficient stock — park the order instead of failing
				order.setStatus(OrderStatus.PENDING_STOCK.name());
				orderRepository.save(order);
				notificationService.notify(WmsEvent.LOW_STOCK,
						"Order #" + order.getOrderNumber() + " is PENDING_STOCK — " +
						"insufficient inventory for product id " + productId + ". A Purchase Order may be needed.");
				return order; // Return gracefully — no exception
			}
		}

		// Deduct inventory for each item (picks from storage locations)
		for (OrderItem item : order.getItems()) {
			inventoryService.deductForOrder(item.getProduct().getId(), item.getQuantity());
		}

		// Create shipment via ShipmentService
		IShipment shipment = shipmentService.createShipment(order);
		if (shipment instanceof com.wms.model.core.Shipment concreteShipment) {
			order.setShipment(concreteShipment);
		}

		order.setStatus(OrderStatus.PROCESSED.name());
		return orderRepository.save(order);
	}

	/** CUSTOMER cancels their order (only CREATED orders can be cancelled). */
	@Override
	public IOrder cancelOrder(Long orderId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

		if (!OrderStatus.CREATED.name().equals(order.getStatus())) {
			throw new IllegalStateException("Only CREATED orders can be cancelled.");
		}

		order.setStatus(OrderStatus.CANCELLED.name());
		IOrder saved = orderRepository.save(order);
		notificationService.notify(WmsEvent.ORDER_CANCELLED, "Order #" + order.getOrderNumber() + " cancelled.");
		return saved;
	}

	// ── helpers ──────────────────────────────────────────────────────────────

	private Customer resolveCustomer(Long customerId) {
		if (customerId == null || customerId <= 0) {
			throw new IllegalArgumentException("Customer id is required.");
		}
		return customerRepository.findById(customerId)
				.orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
	}
}
