package com.wms.model.core;

public interface IShipment {
    Long getId();

    String getShipmentNumber();

    String getStatus();

    void updateStatus(String status);
}
