package com.wms.service;

import com.wms.model.core.IOrder;
import com.wms.model.core.IShipment;

/**
 * ShipmentService — lifecycle management for shipments.
 * SOLID ISP: methods cover all shipment state transitions.
 */
public interface ShipmentService {

	/** Creates a shipment linked to the given order. Called internally by OrderServiceImpl. */
	IShipment createShipment(IOrder order);

	/** Returns shipment details for tracking. */
	IShipment trackShipment(Long shipmentId);

	/**
	 * Staff marks shipment as SHIPPED → also updates linked order to SHIPPED.
	 * Allowed only when shipment.status == CREATED.
	 */
	IShipment markShipped(Long shipmentId);

	/**
	 * Staff marks shipment as DELIVERED → also updates linked order to DELIVERED.
	 * Allowed only when shipment.status == SHIPPED.
	 */
	IShipment markDelivered(Long shipmentId);
}
