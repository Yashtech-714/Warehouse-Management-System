package com.wms.service;

import com.wms.model.core.IPurchaseOrder;
import com.wms.model.core.OrderItem;

import java.util.List;

public interface PurchaseOrderService {
	/** Manager creates a PO addressed to a supplier. */
	IPurchaseOrder createPO(Long supplierId, List<OrderItem> items);

	/** Manager sends PO to supplier → status: SENT_TO_SUPPLIER. */
	IPurchaseOrder sendToSupplier(Long purchaseOrderId);

	/** Supplier marks PO as DELIVERED → triggers stock receipt. */
	IPurchaseOrder deliverPO(Long purchaseOrderId);

	/** Staff receives stock from a DELIVERED PO → updates inventory. */
	IPurchaseOrder receivePO(Long purchaseOrderId, Long storageLocationId);
}
