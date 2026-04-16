package com.wms.model.core;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.wms.model.user.Customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order implements IOrder {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String orderNumber;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "customer_id")
	private Customer customer;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "order_id")
	private List<OrderItem> items = new ArrayList<>();

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "shipment_id")
	private Shipment shipment;

	private LocalDateTime orderDate;

	private String status;

	private BigDecimal totalAmount = BigDecimal.ZERO;

	public Order() {
	}

	public Order(Long id, String orderNumber, Customer customer, List<OrderItem> items,
				 Shipment shipment, LocalDateTime orderDate, String status) {
		this.id = id;
		this.orderNumber = orderNumber;
		this.customer = customer;
		this.items = items == null ? new ArrayList<>() : items;
		this.shipment = shipment;
		this.orderDate = orderDate;
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
	public String getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
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

	public Shipment getShipment() {
		return shipment;
	}

	public void setShipment(Shipment shipment) {
		this.shipment = shipment;
	}

	public LocalDateTime getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(LocalDateTime orderDate) {
		this.orderDate = orderDate;
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
