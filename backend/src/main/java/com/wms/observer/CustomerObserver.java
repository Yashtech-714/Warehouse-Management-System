package com.wms.observer;

import org.springframework.stereotype.Component;

/**
 * CustomerObserver — notifies customers when their order is placed or cancelled.
 */
@Component
public class CustomerObserver implements WmsObserver {

    @Override
    public void onEvent(WmsEvent event, String detail) {
        if (event == WmsEvent.ORDER_PLACED || event == WmsEvent.ORDER_CANCELLED) {
            System.out.println("[CUSTOMER NOTIFICATION] Order update → " + detail);
        }
    }
}
