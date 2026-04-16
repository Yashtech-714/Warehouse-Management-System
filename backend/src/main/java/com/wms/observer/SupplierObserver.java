package com.wms.observer;

import org.springframework.stereotype.Component;

/**
 * SupplierObserver — notifies suppliers when stock is low so they can send a restock.
 */
@Component
public class SupplierObserver implements WmsObserver {

    @Override
    public void onEvent(WmsEvent event, String detail) {
        if (event == WmsEvent.LOW_STOCK) {
            System.out.println("[SUPPLIER NOTIFICATION] Low stock alert → " + detail);
        }
    }
}
