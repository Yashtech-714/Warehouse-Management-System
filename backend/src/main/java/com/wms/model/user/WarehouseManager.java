package com.wms.model.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "warehouse_managers")
public class WarehouseManager extends User {

	private String employeeCode;
	private String managedWarehouseName;

	public WarehouseManager() {
	}

	public WarehouseManager(Long id, String name, String email, String phone, String employeeCode, String managedWarehouseName) {
		super(id, name, email, phone);
		this.employeeCode = employeeCode;
		this.managedWarehouseName = managedWarehouseName;
	}

	public String getEmployeeCode() {
		return employeeCode;
	}

	public void setEmployeeCode(String employeeCode) {
		this.employeeCode = employeeCode;
	}

	public String getManagedWarehouseName() {
		return managedWarehouseName;
	}

	public void setManagedWarehouseName(String managedWarehouseName) {
		this.managedWarehouseName = managedWarehouseName;
	}
}
