package com.restaurant_management.payloads.responses;

import com.restaurant_management.entites.Coupon;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponResponse {
    private String couponCode;
    private String description;
    private double discountPercent;
    private double maxDiscount;
    private double minOrderValue;
    private int availableQuantity;
    private String startDate;
    private String expirationDate;

    public CouponResponse(Coupon coupon) {
        this.couponCode = coupon.getCode();
        this.description = coupon.getDescription();
        this.discountPercent = coupon.getDiscountPercent();
        this.maxDiscount = coupon.getMaxDiscount();
        this.minOrderValue = coupon.getMinOrderValue();
        this.availableQuantity = coupon.getQuantity();
        this.startDate = coupon.getStartDate();
        this.expirationDate = coupon.getExpirationDate();
    }
}
