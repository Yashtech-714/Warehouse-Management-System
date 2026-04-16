package com.wms.facade;

import com.wms.model.core.IOrder;
import com.wms.model.core.InventoryItem;
import com.wms.model.core.OrderItem;
import com.wms.observer.NotificationService;
import com.wms.observer.WmsEvent;
import com.wms.service.InventoryService;
import com.wms.service.OrderService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * OrderFacade — Facade Pattern (Structural Pattern, OOAD requirement h).
 *
 * This class is the single entry-point for order-related workflows.
 * It hides the complexity of coordinating InventoryService, OrderService,
 * and NotificationService behind two clean public methods.
 *
 * Design principles demonstrated:
 *  - SOLID SRP: Facade owns orchestration; individual services own their own logic.
 *  - SOLID DIP: Depends on InventoryService and OrderService interfaces, not impls.
 *  - GRASP Information Expert: Facade knows how the sub-systems fit together.
 *  - GRASP Low Coupling: Controllers only call Facade; they don't touch services directly.
 */
@Service
public class OrderFacade {

    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;

    public OrderFacade(OrderService orderService,
                       InventoryService inventoryService,
                       NotificationService notificationService) {
        this.orderService = orderService;
        this.inventoryService = inventoryService;
        this.notificationService = notificationService;
    }

    /**
     * placeOrder — Facade method that:
     *  1. Creates the order via OrderService (uses Factory + Builder internally).
     *  2. Checks inventory for each item and fires LOW_STOCK if threshold is hit.
     *  3. Fires ORDER_PLACED observer event.
     *
     * @param customerId ID of the customer placing the order
     * @param items      list of items in the order
     * @return the persisted IOrder
     */
    public IOrder placeOrder(Long customerId, List<OrderItem> items) {
        IOrder order = orderService.createOrder(customerId, items);

        // Check for low stock after order creation and notify observers
        for (OrderItem item : items) {
            if (item.getProduct() != null) {
                Long productId = item.getProduct().getId();
                checkAndNotifyLowStock(productId);
            }
        }

        notificationService.notify(
                WmsEvent.ORDER_PLACED,
                "Order #" + order.getOrderNumber() + " placed by customer " + customerId
        );

        return order;
    }

    /**
     * cancelOrder — Facade method that:
     *  1. Cancels the order via OrderService.
     *  2. Fires ORDER_CANCELLED observer event.
     *
     * @param orderId the ID of the order to cancel
     * @return the updated IOrder with CANCELLED status
     */
    public IOrder cancelOrder(Long orderId) {
        IOrder order = orderService.cancelOrder(orderId);

        notificationService.notify(
                WmsEvent.ORDER_CANCELLED,
                "Order #" + order.getOrderNumber() + " has been cancelled"
        );

        return order;
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private void checkAndNotifyLowStock(Long productId) {
        List<InventoryItem> items = inventoryService.getAllInventoryItems()
                .stream()
                .filter(i -> i.getProduct() != null
                        && productId.equals(i.getProduct().getId()))
                .toList();

        int totalQty = items.stream()
                .mapToInt(i -> i.getQuantity() == null ? 0 : i.getQuantity())
                .sum();
        int reorderLevel = items.stream()
                .mapToInt(i -> i.getReorderLevel() == null ? 0 : i.getReorderLevel())
                .max()
                .orElse(0);

        if (totalQty <= reorderLevel) {
            String productName = items.isEmpty() ? "Product#" + productId
                    : items.get(0).getProduct().getName();
            notificationService.notify(
                    WmsEvent.LOW_STOCK,
                    productName + " is low (" + totalQty + " units remaining, reorder level: " + reorderLevel + ")"
            );
        }
    }
}
