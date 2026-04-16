package com.wms.service.impl;

import com.wms.factory.PurchaseOrderFactory;
import com.wms.model.core.IPurchaseOrder;
import com.wms.model.core.OrderItem;
import com.wms.model.core.Product;
import com.wms.model.core.PurchaseOrder;
import com.wms.model.core.enums.PurchaseOrderStatus;
import com.wms.model.user.Supplier;
import com.wms.observer.NotificationService;
import com.wms.observer.WmsEvent;
import com.wms.repository.OrderRepository;
import com.wms.repository.ProductRepository;
import com.wms.repository.PurchaseOrderRepository;
import com.wms.repository.SupplierRepository;
import com.wms.service.InventoryService;
import com.wms.service.PurchaseOrderService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * PurchaseOrderServiceImpl — business logic for PO lifecycle.
 * SOLID SRP: only PO logic here.
 * Observer pattern: fires LOW_STOCK / ORDER_PLACED events via NotificationService.
 */
@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

	private final PurchaseOrderFactory purchaseOrderFactory;
	private final PurchaseOrderRepository purchaseOrderRepository;
	private final SupplierRepository supplierRepository;
	private final ProductRepository productRepository;
	private final InventoryService inventoryService;
	private final NotificationService notificationService;
	private final OrderRepository orderRepository;

	public PurchaseOrderServiceImpl(PurchaseOrderFactory purchaseOrderFactory,
	                                 PurchaseOrderRepository purchaseOrderRepository,
	                                 SupplierRepository supplierRepository,
	                                 ProductRepository productRepository,
	                                 InventoryService inventoryService,
	                                 NotificationService notificationService,
	                                 OrderRepository orderRepository) {
		this.purchaseOrderFactory = purchaseOrderFactory;
		this.purchaseOrderRepository = purchaseOrderRepository;
		this.supplierRepository = supplierRepository;
		this.productRepository = productRepository;
		this.inventoryService = inventoryService;
		this.notificationService = notificationService;
		this.orderRepository = orderRepository;
	}

	@Override
	public IPurchaseOrder createPO(Long supplierId, List<OrderItem> items) {
		if (items == null || items.isEmpty()) {
			throw new IllegalArgumentException("Purchase order requires at least one item.");
		}
		Supplier supplier = resolveSupplier(supplierId);
		List<OrderItem> resolvedItems = resolveItems(items);
		IPurchaseOrder po = purchaseOrderFactory.createPO(supplier, resolvedItems);
		if (!(po instanceof PurchaseOrder purchaseOrder)) {
			throw new IllegalStateException("PurchaseOrderFactory must return PurchaseOrder instance.");
		}
		return purchaseOrderRepository.save(purchaseOrder);
	}

	@Override
	public IPurchaseOrder sendToSupplier(Long purchaseOrderId) {
		PurchaseOrder po = findPO(purchaseOrderId);
		po.setStatus(PurchaseOrderStatus.SENT_TO_SUPPLIER.name());
		return purchaseOrderRepository.save(po);
	}

	/**
	 * Supplier marks PO as DELIVERED.
	 * Business rule: PO must be in SENT_TO_SUPPLIER state.
	 */
	@Override
	public IPurchaseOrder deliverPO(Long purchaseOrderId) {
		PurchaseOrder po = findPO(purchaseOrderId);
		if (!PurchaseOrderStatus.SENT_TO_SUPPLIER.name().equals(po.getStatus())) {
			throw new IllegalStateException("Only SENT_TO_SUPPLIER purchase orders can be marked as delivered.");
		}
		po.setStatus(PurchaseOrderStatus.DELIVERED.name());
		notificationService.notify(WmsEvent.ORDER_PLACED,
				"PO #" + po.getPurchaseOrderNumber() + " marked DELIVERED by supplier.");
		return purchaseOrderRepository.save(po);
	}

	/**
	 * Staff receives stock from a DELIVERED PO into a storage location.
	 * Business rule: PO must be in DELIVERED state.
	 * Inventory updated here — this is the ONLY way to add stock.
	 * AUTO-RECOVERY: after restocking, PENDING_STOCK orders are re-queued if stock is now sufficient.
	 */
	@Override
	public IPurchaseOrder receivePO(Long purchaseOrderId, Long storageLocationId) {
		PurchaseOrder po = findPO(purchaseOrderId);
		if (!PurchaseOrderStatus.DELIVERED.name().equals(po.getStatus())) {
			throw new IllegalStateException("Only DELIVERED purchase orders can be received.");
		}
		for (OrderItem item : po.getItems()) {
			Long productId = item.getProduct() == null ? null : item.getProduct().getId();
			if (productId == null || item.getQuantity() == null) continue;
			inventoryService.addStock(productId, storageLocationId, item.getQuantity());
			notificationService.notify(WmsEvent.LOW_STOCK,
					"Stock received for product #" + productId + " qty=" + item.getQuantity());

			// ── Auto-recovery: restore PENDING_STOCK orders that can now be fulfilled ──
			autoRecoverPendingOrders(productId);
		}
		po.setStatus("RECEIVED");
		return purchaseOrderRepository.save(po);
	}

	/**
	 * After stock is added, finds all PENDING_STOCK orders containing the given product
	 * and restores them to CREATED if inventory is now sufficient.
	 * Observer pattern: fires notification to inform Manager and Customer.
	 */
	private void autoRecoverPendingOrders(Long productId) {
		orderRepository.findByStatus("PENDING_STOCK").forEach(order -> {
			boolean orderNeedsProduct = order.getItems().stream()
					.anyMatch(i -> i.getProduct() != null && productId.equals(i.getProduct().getId()));
			if (!orderNeedsProduct) return;

			// Check if all items in this order now have sufficient stock
			boolean allSufficient = order.getItems().stream().allMatch(i -> {
				Long pid = i.getProduct() == null ? null : i.getProduct().getId();
				Integer qty = i.getQuantity();
				return pid != null && qty != null && inventoryService.checkAvailability(pid, qty);
			});

			if (allSufficient) {
				order.setStatus("CREATED"); // Back to processable
				orderRepository.save(order);
				notificationService.notify(WmsEvent.ORDER_PLACED,
						"RESTOCKED: Order #" + order.getOrderNumber() +
						" is now ready to process — sufficient stock available.");
			}
		});
	}

	// ── private helpers ─────────────────────────────────────────────────────

	private PurchaseOrder findPO(Long id) {
		return purchaseOrderRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Purchase order not found: " + id));
	}

	private Supplier resolveSupplier(Long supplierId) {
		if (supplierId == null || supplierId <= 0) throw new IllegalArgumentException("Supplier id is required.");
		return supplierRepository.findById(supplierId)
				.orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + supplierId));
	}

	private List<OrderItem> resolveItems(List<OrderItem> items) {
		List<OrderItem> resolved = new ArrayList<>();
		for (OrderItem item : items) {
			Long productId = item.getProduct() == null ? null : item.getProduct().getId();
			if (productId == null) throw new IllegalArgumentException("Each PO item requires a valid product id.");
			Product product = productRepository.findById(productId)
					.orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
			int qty = item.getQuantity() == null ? 0 : item.getQuantity();
			if (qty <= 0) throw new IllegalArgumentException("Item quantity must be greater than zero.");
			OrderItem resolved1 = new OrderItem();
			resolved1.setProduct(product);
			resolved1.setQuantity(qty);
			resolved1.setUnitPrice(product.getUnitPrice() == null ? BigDecimal.ZERO : product.getUnitPrice());
			resolved.add(resolved1);
		}
		return resolved;
	}
}
