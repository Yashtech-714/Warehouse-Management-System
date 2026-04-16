package com.wms.model.core;

import java.math.BigDecimal;
import java.util.List;

public interface IPurchaseOrder {
    Long getId();

    String getPurchaseOrderNumber();

    List<OrderItem> getItems();

    void setItems(List<OrderItem> items);

    String getStatus();

    BigDecimal getTotalAmount();

    BigDecimal calculateTotalAmount();
}
