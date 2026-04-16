package com.wms.model.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipments")
public class Shipment implements IShipment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String shipmentNumber;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_ref_id")
	@JsonIgnore
	private Order order;

	private String shippingAddress;
	private LocalDateTime shippedAt;
	private LocalDateTime estimatedDeliveryAt;
	private LocalDateTime deliveredAt;
	private String status;

	public Shipment() {
	}

	public Shipment(Long id, String shipmentNumber, Order order, String shippingAddress, LocalDateTime shippedAt,
					LocalDateTime estimatedDeliveryAt, LocalDateTime deliveredAt, String status) {
		this.id = id;
		this.shipmentNumber = shipmentNumber;
		this.order = order;
		this.shippingAddress = shippingAddress;
		this.shippedAt = shippedAt;
		this.estimatedDeliveryAt = estimatedDeliveryAt;
		this.deliveredAt = deliveredAt;
		this.status = status;
	}

	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String getShipmentNumber() {
		return shipmentNumber;
	}

	public void setShipmentNumber(String shipmentNumber) {
		this.shipmentNumber = shipmentNumber;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public String getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(String shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	public LocalDateTime getShippedAt() {
		return shippedAt;
	}

	public void setShippedAt(LocalDateTime shippedAt) {
		this.shippedAt = shippedAt;
	}

	public LocalDateTime getEstimatedDeliveryAt() {
		return estimatedDeliveryAt;
	}

	public void setEstimatedDeliveryAt(LocalDateTime estimatedDeliveryAt) {
		this.estimatedDeliveryAt = estimatedDeliveryAt;
	}

	public LocalDateTime getDeliveredAt() {
		return deliveredAt;
	}

	public void setDeliveredAt(LocalDateTime deliveredAt) {
		this.deliveredAt = deliveredAt;
	}

	@Override
	public String getStatus() {
		return status;
	}

	@Override
	public void updateStatus(String status) {
		this.status = status;
	}
}
