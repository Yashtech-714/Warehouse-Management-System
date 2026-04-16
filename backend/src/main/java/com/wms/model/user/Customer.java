package com.wms.model.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
public class Customer extends User {

	private String customerCode;

	public Customer() {
	}

	public Customer(Long id, String name, String email, String phone, String customerCode) {
		super(id, name, email, phone);
		this.customerCode = customerCode;
	}

	public String getCustomerCode() {
		return customerCode;
	}

	public void setCustomerCode(String customerCode) {
		this.customerCode = customerCode;
	}
}
