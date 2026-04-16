package com.wms.builder;

import com.wms.model.core.Order;
import com.wms.model.core.OrderItem;
import com.wms.model.core.Product;
import com.wms.model.user.Customer;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class OrderBuilder implements Builder {
	private Order order;

	public OrderBuilder() {
		this.order = new Order();
	}

	public OrderBuilder setCustomer(Customer customer) {
		order.setCustomer(customer);
		return this;
	}

	public OrderBuilder addItem(Product product, int quantity) {
		OrderItem item = new OrderItem();
		item.setProduct(product);
		item.setQuantity(quantity);
		item.setUnitPrice(product != null ? product.getUnitPrice() : null);

		if (order.getItems() == null) {
			order.setItems(new ArrayList<>());
		}
		order.getItems().add(item);
		order.refreshTotalAmount();
		return this;
	}

	@Override
	public void buildPart() {
		if (order.getOrderNumber() == null) {
			order.setOrderNumber("ORD-" + System.currentTimeMillis());
		}
		if (order.getOrderDate() == null) {
			order.setOrderDate(LocalDateTime.now());
		}
		if (order.getStatus() == null) {
			order.setStatus("CREATED");
		}
		if (order.getItems() == null) {
			order.setItems(new ArrayList<>());
		}
		order.refreshTotalAmount();
	}

	public Order getResult() {
		return order;
	}
}

