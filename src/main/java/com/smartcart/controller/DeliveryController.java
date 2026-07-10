package com.smartcart.controller;

import com.smartcart.dto.ApiResponse;
import com.smartcart.dto.DeliveryCheckRequest;
import com.smartcart.model.Product;
import com.smartcart.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {
    private final ProductRepository productRepository;

    public DeliveryController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @PostMapping("/check")
    public ResponseEntity<ApiResponse<Map<String, Object>>> check(@RequestBody DeliveryCheckRequest request) {
        Product product = productRepository.findById(request.getProductId()).orElse(null);
        if (product == null) return ResponseEntity.badRequest().body(ApiResponse.fail("Product not found"));
        String pins = product.getAvailablePincodes() == null ? "" : product.getAvailablePincodes();
        boolean available = pins.contains(request.getPincode());
        Map<String, Object> data = new HashMap<>();
        data.put("available", available);
        data.put("pincode", request.getPincode());
        data.put("deliveryCharge", product.getDeliveryCharge());
        data.put("estimatedDate", LocalDate.now().plusDays(product.getEstimatedDeliveryDays() == null ? 4 : product.getEstimatedDeliveryDays()).toString());
        data.put("message", available ? "Delivery available for this PIN code" : "Currently delivery is not available for this PIN code");
        return ResponseEntity.ok(ApiResponse.ok("Delivery check completed", data));
    }
}
