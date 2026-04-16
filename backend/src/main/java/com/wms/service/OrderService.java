package com.wms.service;

import com.wms.model.core.IOrder;
import com.wms.model.core.OrderItem;

import java.util.List;

public interface OrderService {
	IOrder createOrder(Long customerId, List<OrderItem> items);

	IOrder processOrder(Long orderId);

	IOrder cancelOrder(Long orderId);
}

