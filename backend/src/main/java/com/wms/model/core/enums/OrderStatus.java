package com.wms.model.core.enums;

public enum OrderStatus {
    CREATED,
    PENDING_STOCK,   // insufficient inventory at process time — awaiting restock
    PROCESSED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
