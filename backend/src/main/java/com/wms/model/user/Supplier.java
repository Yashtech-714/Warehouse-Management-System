package com.wms.model.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "suppliers")
public class Supplier extends User {

	private String supplierCode;
	private String companyName;

	public Supplier() {
	}

	public Supplier(Long id, String name, String email, String phone, String supplierCode, String companyName) {
		super(id, name, email, phone);
		this.supplierCode = supplierCode;
		this.companyName = companyName;
	}

	public String getSupplierCode() {
		return supplierCode;
	}

	public void setSupplierCode(String supplierCode) {
		this.supplierCode = supplierCode;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
}
