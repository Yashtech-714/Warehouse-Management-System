package com.wms.service.impl;

import com.wms.factory.ShipmentFactory;
import com.wms.model.core.IOrder;
import com.wms.model.core.IShipment;
import com.wms.model.core.Order;
import com.wms.model.core.Shipment;
import com.wms.repository.OrderRepository;
import com.wms.repository.ShipmentRepository;
import com.wms.service.ShipmentService;
import org.springframework.stereotype.Service;

/**
 * ShipmentServiceImpl — full shipment lifecycle.
 * SOLID SRP: only shipment state transitions here.
 * GRASP Information Expert: owns all shipment mutation.
 *
 * Status transitions:
 *   CREATED → SHIPPED   (Staff: markShipped)
 *   SHIPPED → DELIVERED (Staff: markDelivered)
 *
 * Both transitions also sync the linked order's status.
 */
@Service
public class ShipmentServiceImpl implements ShipmentService {

	private static final String STATUS_CREATED   = "CREATED";
	private static final String STATUS_SHIPPED   = "SHIPPED";
	private static final String STATUS_DELIVERED = "DELIVERED";

	private final ShipmentFactory    shipmentFactory;
	private final ShipmentRepository shipmentRepository;
	private final OrderRepository    orderRepository;

	public ShipmentServiceImpl(ShipmentFactory shipmentFactory,
	                            ShipmentRepository shipmentRepository,
	                            OrderRepository orderRepository) {
		this.shipmentFactory    = shipmentFactory;
		this.shipmentRepository = shipmentRepository;
		this.orderRepository    = orderRepository;
	}

	/** Creates a shipment linked to the given order (called by OrderServiceImpl). */
	@Override
	public IShipment createShipment(IOrder order) {
		if (order == null || order.getId() == null) {
			throw new IllegalArgumentException("Order id is required to create shipment.");
		}
		Order managedOrder = findOrder(order.getId());
		IShipment created = shipmentFactory.createShipment(managedOrder);
		if (!(created instanceof Shipment shipment)) {
			throw new IllegalStateException("ShipmentFactory must return Shipment instance.");
		}
		shipment.setOrder(managedOrder);
		return shipmentRepository.save(shipment);
	}

	/** Returns shipment details for tracking. */
	@Override
	public IShipment trackShipment(Long shipmentId) {
		return findShipment(shipmentId);
	}

	/**
	 * Staff marks shipment as SHIPPED.
	 * Also propagates SHIPPED status to the linked order.
	 */
	@Override
	public IShipment markShipped(Long shipmentId) {
		Shipment shipment = findShipment(shipmentId);
		if (!STATUS_CREATED.equals(shipment.getStatus())) {
			throw new IllegalStateException(
					"Only CREATED shipments can be marked as SHIPPED. Current: " + shipment.getStatus());
		}
		shipment.updateStatus(STATUS_SHIPPED);
		shipment.setShippedAt(java.time.LocalDateTime.now());
		shipmentRepository.save(shipment);

		// Sync order status
		if (shipment.getOrder() != null) {
			Order order = findOrder(shipment.getOrder().getId());
			order.setStatus(STATUS_SHIPPED);
			orderRepository.save(order);
		}
		return shipment;
	}

	/**
	 * Staff marks shipment as DELIVERED.
	 * Also propagates DELIVERED status to the linked order.
	 */
	@Override
	public IShipment markDelivered(Long shipmentId) {
		Shipment shipment = findShipment(shipmentId);
		if (!STATUS_SHIPPED.equals(shipment.getStatus())) {
			throw new IllegalStateException(
					"Only SHIPPED shipments can be marked as DELIVERED. Current: " + shipment.getStatus());
		}
		shipment.updateStatus(STATUS_DELIVERED);
		shipment.setDeliveredAt(java.time.LocalDateTime.now());
		shipmentRepository.save(shipment);

		// Sync order status
		if (shipment.getOrder() != null) {
			Order order = findOrder(shipment.getOrder().getId());
			order.setStatus(STATUS_DELIVERED);
			orderRepository.save(order);
		}
		return shipment;
	}

	// ── private helpers ──────────────────────────────────────────────────────

	private Shipment findShipment(Long id) {
		return shipmentRepository.findById(id)
				.map(s -> {
					if (!(s instanceof Shipment)) throw new IllegalStateException("Invalid shipment type.");
					return (Shipment) s;
				})
				.orElseThrow(() -> new IllegalArgumentException("Shipment not found: " + id));
	}

	private Order findOrder(Long id) {
		return orderRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
	}
}
