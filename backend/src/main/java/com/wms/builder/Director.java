package com.wms.builder;

public class Director {
	private final Builder builder;

	public Director(Builder builder) {
		this.builder = builder;
	}

	public void construct() {
		builder.buildPart();
	}
}

