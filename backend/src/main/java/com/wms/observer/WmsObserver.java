package com.wms.observer;

/**
 * WmsObserver — Observer interface (Behavioral Pattern: Observer).
 *
 * SOLID — Open/Closed Principle: New observers can be added
 * without modifying NotificationService (the Subject).
 *
 * SOLID — Dependency Inversion: NotificationService depends on
 * this abstraction, not on concrete observer classes.
 */
public interface WmsObserver {

    /**
     * Called by the Subject (NotificationService) when a domain event occurs.
     *
     * @param event  the type of event that occurred
     * @param detail human-readable description of the event
     */
    void onEvent(WmsEvent event, String detail);
}
