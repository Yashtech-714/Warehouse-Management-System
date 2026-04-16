package com.wms.factory;

import com.wms.model.core.IOrder;
import com.wms.model.core.Order;
import com.wms.model.core.OrderItem;
import com.wms.model.user.Customer;

import java.time.LocalDateTime;
import java.util.List;

public class OrderFactory {
	public IOrder createOrder(Customer customer, List<OrderItem> items) {
		Order order = new Order();
		order.setOrderNumber("ORD-" + System.currentTimeMillis());
		order.setCustomer(customer);
		order.setItems(items);
		order.setOrderDate(LocalDateTime.now());
		order.setStatus("CREATED");
		order.refreshTotalAmount();
		return order;
	}
}

