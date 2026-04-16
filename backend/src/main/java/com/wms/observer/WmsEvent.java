package com.wms.observer;

/**
 * WmsEvent — Enum of domain events in the WMS system.
 * Used by the Observer pattern to identify what happened.
 * Satisfies (h) Behavioral Pattern — Observer.
 */
public enum WmsEvent {
    ORDER_PLACED,
    ORDER_CANCELLED,
    LOW_STOCK
}
