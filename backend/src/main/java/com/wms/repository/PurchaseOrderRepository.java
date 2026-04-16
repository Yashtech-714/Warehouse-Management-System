package com.wms.repository;

import com.wms.model.core.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
	Optional<PurchaseOrder> findByPurchaseOrderNumber(String purchaseOrderNumber);

	List<PurchaseOrder> findByStatus(String status);
}
