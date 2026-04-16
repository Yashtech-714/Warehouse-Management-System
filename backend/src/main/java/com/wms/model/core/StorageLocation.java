package com.wms.model.core;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "storage_locations")
public class StorageLocation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private String code;

	private String zone;
	private String aisle;
	private String rack;
	private String bin;

	public StorageLocation() {
	}

	public StorageLocation(Long id, String code, String zone, String aisle, String rack, String bin) {
		this.id = id;
		this.code = code;
		this.zone = zone;
		this.aisle = aisle;
		this.rack = rack;
		this.bin = bin;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}

	public String getAisle() {
		return aisle;
	}

	public void setAisle(String aisle) {
		this.aisle = aisle;
	}

	public String getRack() {
		return rack;
	}

	public void setRack(String rack) {
		this.rack = rack;
	}

	public String getBin() {
		return bin;
	}

	public void setBin(String bin) {
		this.bin = bin;
	}
}
