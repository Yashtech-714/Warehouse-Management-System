package com.wms.model.core;

import com.wms.model.user.Customer;
import com.wms.model.user.Supplier;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder implements IPurchaseOrder {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String purchaseOrderNumber;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "supplier_id")
	private Supplier supplier;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "purchase_order_id")
	private List<OrderItem> items = new ArrayList<>();

	private LocalDate orderDate;
	private LocalDate expectedDeliveryDate;
	private String status;
	private BigDecimal totalAmount = BigDecimal.ZERO;

	public PurchaseOrder() {
	}

	public PurchaseOrder(Long id, String purchaseOrderNumber, Supplier supplier, List<OrderItem> items,
						 LocalDate orderDate, LocalDate expectedDeliveryDate, String status) {
		this.id = id;
		this.purchaseOrderNumber = purchaseOrderNumber;
		this.supplier = supplier;
		this.items = items == null ? new ArrayList<>() : items;
		this.orderDate = orderDate;
		this.expectedDeliveryDate = expectedDeliveryDate;
		this.status = status;
		this.totalAmount = calculateTotalAmount();
	}

	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String getPurchaseOrderNumber() {
		return purchaseOrderNumber;
	}

	public void setPurchaseOrderNumber(String purchaseOrderNumber) {
		this.purchaseOrderNumber = purchaseOrderNumber;
	}

	public Supplier getSupplier() {
		return supplier;
	}

	public void setSupplier(Supplier supplier) {
		this.supplier = supplier;
	}

	@Override
	public List<OrderItem> getItems() {
		return items;
	}

	@Override
	public void setItems(List<OrderItem> items) {
		this.items = items == null ? new ArrayList<>() : items;
		this.totalAmount = calculateTotalAmount();
	}

	public LocalDate getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(LocalDate orderDate) {
		this.orderDate = orderDate;
	}

	public LocalDate getExpectedDeliveryDate() {
		return expectedDeliveryDate;
	}

	public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
		this.expectedDeliveryDate = expectedDeliveryDate;
	}

	@Override
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	@Override
	public BigDecimal calculateTotalAmount() {
		return items.stream()
				.map(OrderItem::getLineTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public void refreshTotalAmount() {
		this.totalAmount = calculateTotalAmount();
	}
}
