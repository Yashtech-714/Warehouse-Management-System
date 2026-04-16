package com.wms.model.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "warehouse_staff")
public class WarehouseStaff extends User {

	private String employeeCode;
	private String shift;

	public WarehouseStaff() {
	}

	public WarehouseStaff(Long id, String name, String email, String phone, String employeeCode, String shift) {
		super(id, name, email, phone);
		this.employeeCode = employeeCode;
		this.shift = shift;
	}

	public String getEmployeeCode() {
		return employeeCode;
	}

	public void setEmployeeCode(String employeeCode) {
		this.employeeCode = employeeCode;
	}

	public String getShift() {
		return shift;
	}

	public void setShift(String shift) {
		this.shift = shift;
	}
}
