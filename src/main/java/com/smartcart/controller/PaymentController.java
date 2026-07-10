package com.smartcart.controller;

import com.smartcart.dto.ApiResponse;
import com.smartcart.dto.PaymentRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    @PostMapping("/dummy")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dummy(@RequestBody PaymentRequest request) {
        Map<String, Object> data = new HashMap<>();
        String method = request.getMethod() == null ? "PhonePe" : request.getMethod();
        data.put("paymentMethod", method);
        data.put("paymentStatus", "PAID");
        data.put("transactionId", method.toUpperCase().replace(" ", "-") + "-DEMO-" + System.currentTimeMillis());
        data.put("amount", request.getAmount());
        data.put("message", method + " dummy payment completed successfully");
        return ResponseEntity.ok(ApiResponse.ok("Payment successful", data));
    }
}
