package com.wms.model.core;

import com.wms.model.user.WarehouseManager;

import java.time.LocalDateTime;

public class Report {
	private Long id;
	private String title;
	private String type;
	private String content;
	private LocalDateTime generatedAt;
	private WarehouseManager generatedBy;

	public Report() {
	}

	public Report(Long id, String title, String type, String content, LocalDateTime generatedAt, WarehouseManager generatedBy) {
		this.id = id;
		this.title = title;
		this.type = type;
		this.content = content;
		this.generatedAt = generatedAt;
		this.generatedBy = generatedBy;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public LocalDateTime getGeneratedAt() {
		return generatedAt;
	}

	public void setGeneratedAt(LocalDateTime generatedAt) {
		this.generatedAt = generatedAt;
	}

	public WarehouseManager getGeneratedBy() {
		return generatedBy;
	}

	public void setGeneratedBy(WarehouseManager generatedBy) {
		this.generatedBy = generatedBy;
	}
}

