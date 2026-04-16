package com.wms.model.core;

import java.math.BigDecimal;
import java.util.List;

public interface IOrder {
    Long getId();

    String getOrderNumber();

    List<OrderItem> getItems();

    void setItems(List<OrderItem> items);

    String getStatus();

    BigDecimal getTotalAmount();

    BigDecimal calculateTotalAmount();
}
