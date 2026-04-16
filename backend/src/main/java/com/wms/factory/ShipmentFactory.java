package com.wms.factory;

import com.wms.model.core.IOrder;
import com.wms.model.core.IShipment;
import com.wms.model.core.Order;
import com.wms.model.core.Shipment;

import java.time.LocalDateTime;

public class ShipmentFactory {
	public IShipment createShipment(IOrder order) {
		Shipment shipment = new Shipment();
		shipment.setShipmentNumber("SHP-" + System.currentTimeMillis());
		if (order instanceof Order concreteOrder) {
			shipment.setOrder(concreteOrder);
		}
		shipment.setShippedAt(LocalDateTime.now());
		shipment.updateStatus("CREATED");
		return shipment;
	}
}

