package com.wms.config;

import com.wms.factory.OrderFactory;
import com.wms.factory.PurchaseOrderFactory;
import com.wms.factory.ShipmentFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

	@Bean
	public OrderFactory orderFactory() {
		return new OrderFactory();
	}

	@Bean
	public ShipmentFactory shipmentFactory() {
		return new ShipmentFactory();
	}

	@Bean
	public PurchaseOrderFactory purchaseOrderFactory() {
		return new PurchaseOrderFactory();
	}
}

