package com.wms.observer;

import org.springframework.stereotype.Component;

/**
 * ManagerObserver — notifies the Warehouse Manager of all events.
 * Implements WmsObserver (SOLID: DIP — depends on abstraction).
 */
@Component
public class ManagerObserver implements WmsObserver {

    @Override
    public void onEvent(WmsEvent event, String detail) {
        System.out.println("[MANAGER NOTIFICATION] " + event + " → " + detail);
    }
}
