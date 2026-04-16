package com.wms.model.core;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "product_id")
	private Product product;

	private Integer quantity;

	private BigDecimal unitPrice;

	public OrderItem() {
	}

	public OrderItem(Long id, Product product, Integer quantity, BigDecimal unitPrice) {
		this.id = id;
		this.product = product;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}

	public BigDecimal getLineTotal() {
		BigDecimal safePrice = unitPrice == null ? BigDecimal.ZERO : unitPrice;
		int safeQuantity = quantity == null ? 0 : quantity;
		return safePrice.multiply(BigDecimal.valueOf(safeQuantity));
	}
}
