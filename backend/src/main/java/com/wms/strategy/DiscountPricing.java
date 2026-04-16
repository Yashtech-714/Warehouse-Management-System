package com.wms.strategy;

public class DiscountPricing implements PricingStrategy {
	private static final double DISCOUNT_RATE = 0.10;

	@Override
	public double calculatePrice(double basePrice) {
		return basePrice - (basePrice * DISCOUNT_RATE);
	}
}
