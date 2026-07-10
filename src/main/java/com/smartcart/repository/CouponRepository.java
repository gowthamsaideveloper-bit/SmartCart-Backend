package com.smartcart.repository;

import com.smartcart.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    List<Coupon> findByActiveTrue();
    Optional<Coupon> findByCodeIgnoreCaseAndActiveTrue(String code);
}
