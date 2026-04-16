package com.wms.factory;

import com.wms.model.core.IPurchaseOrder;
import com.wms.model.core.OrderItem;
import com.wms.model.core.PurchaseOrder;
import com.wms.model.user.Supplier;

import java.time.LocalDate;
import java.util.List;

public class PurchaseOrderFactory {
	public IPurchaseOrder createPO(Supplier supplier, List<OrderItem> items) {
		PurchaseOrder purchaseOrder = new PurchaseOrder();
		purchaseOrder.setPurchaseOrderNumber("PO-" + System.currentTimeMillis());
		purchaseOrder.setSupplier(supplier);
		purchaseOrder.setItems(items);
		purchaseOrder.setOrderDate(LocalDate.now());
		purchaseOrder.setStatus("SENT_TO_SUPPLIER");
		purchaseOrder.refreshTotalAmount();
		return purchaseOrder;
	}
}

