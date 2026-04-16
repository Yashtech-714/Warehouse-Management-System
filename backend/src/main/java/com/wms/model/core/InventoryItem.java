package com.wms.model.core;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "product_id")
	private Product product;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "storage_location_id")
	private StorageLocation storageLocation;
	private Integer quantity;
	private Integer reorderLevel;

	public InventoryItem() {
	}

	public InventoryItem(Long id, Product product, StorageLocation storageLocation, Integer quantity, Integer reorderLevel) {
		this.id = id;
		this.product = product;
		this.storageLocation = storageLocation;
		this.quantity = quantity;
		this.reorderLevel = reorderLevel;
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

	public StorageLocation getStorageLocation() {
		return storageLocation;
	}

	public void setStorageLocation(StorageLocation storageLocation) {
		this.storageLocation = storageLocation;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Integer getReorderLevel() {
		return reorderLevel;
	}

	public void setReorderLevel(Integer reorderLevel) {
		this.reorderLevel = reorderLevel;
	}
}

