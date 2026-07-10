package com.smartcart.controller;

import com.smartcart.dto.ApiResponse;
import com.smartcart.model.Coupon;
import com.smartcart.repository.CouponRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {
    private final CouponRepository couponRepository;

    public CouponController(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Coupon>>> active() {
        return ResponseEntity.ok(ApiResponse.ok("Coupons fetched", couponRepository.findByActiveTrue()));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validate(@RequestParam String code, @RequestParam BigDecimal amount) {
        Coupon coupon = couponRepository.findByCodeIgnoreCaseAndActiveTrue(code).orElse(null);
        if (coupon == null) return ResponseEntity.badRequest().body(ApiResponse.fail("Invalid coupon code"));
        if (amount.compareTo(coupon.getMinOrderAmount()) < 0) return ResponseEntity.badRequest().body(ApiResponse.fail("Minimum order value is " + coupon.getMinOrderAmount()));
        BigDecimal discount = amount.multiply(BigDecimal.valueOf(coupon.getDiscountPercent())).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        if (coupon.getMaxDiscountAmount() != null && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) discount = coupon.getMaxDiscountAmount();
        Map<String, Object> data = new HashMap<>();
        data.put("coupon", coupon);
        data.put("discount", discount);
        data.put("message", coupon.getDiscountPercent() + "% discount applied");
        return ResponseEntity.ok(ApiResponse.ok("Coupon applied", data));
    }
}
