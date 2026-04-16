package com.wms.observer;

import org.springframework.stereotype.Component;

/**
 * StaffObserver — notifies warehouse staff when an order is placed so they can process it.
 */
@Component
public class StaffObserver implements WmsObserver {

    @Override
    public void onEvent(WmsEvent event, String detail) {
        if (event == WmsEvent.ORDER_PLACED) {
            System.out.println("[STAFF NOTIFICATION] New order to process → " + detail);
        }
    }
}
