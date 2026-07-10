package com.smartcart.controller;

import com.smartcart.config.CurrentUser;
import com.smartcart.dto.ApiResponse;
import com.smartcart.dto.PlaceOrderRequest;
import com.smartcart.model.*;
import com.smartcart.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final CouponRepository couponRepository;
    private final PaymentRepository paymentRepository;
    private final TrackingUpdateRepository trackingUpdateRepository;
    private final NotificationRepository notificationRepository;
    private final CurrentUser currentUser;

    public OrderController(OrderRepository orderRepository, CartItemRepository cartItemRepository, CouponRepository couponRepository, PaymentRepository paymentRepository, TrackingUpdateRepository trackingUpdateRepository, NotificationRepository notificationRepository, CurrentUser currentUser) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.couponRepository = couponRepository;
        this.paymentRepository = paymentRepository;
        this.trackingUpdateRepository = trackingUpdateRepository;
        this.notificationRepository = notificationRepository;
        this.currentUser = currentUser;
    }

    @PostMapping("/place")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> place(HttpServletRequest httpRequest, @RequestBody PlaceOrderRequest request) {
        User user = currentUser.get(httpRequest);
        List<CartItem> cart = cartItemRepository.findByUserIdAndSavedForLaterFalse(user.getId());
        if (cart.isEmpty()) return ResponseEntity.badRequest().body(ApiResponse.fail("Cart is empty"));

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal delivery = BigDecimal.ZERO;
        for (CartItem cartItem : cart) {
            total = total.add(cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            delivery = delivery.add(cartItem.getProduct().getDeliveryCharge() == null ? BigDecimal.ZERO : cartItem.getProduct().getDeliveryCharge());
        }

        BigDecimal discount = BigDecimal.ZERO;
        String couponCode = request.getCouponCode();
        if (couponCode != null && !couponCode.isBlank()) {
            Coupon coupon = couponRepository.findByCodeIgnoreCaseAndActiveTrue(couponCode).orElse(null);
            if (coupon != null && total.compareTo(coupon.getMinOrderAmount()) >= 0) {
                discount = total.multiply(BigDecimal.valueOf(coupon.getDiscountPercent())).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                if (coupon.getMaxDiscountAmount() != null && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) discount = coupon.getMaxDiscountAmount();
            }
        }
        if (total.compareTo(BigDecimal.valueOf(999)) >= 0) delivery = BigDecimal.ZERO;

        CustomerOrder order = new CustomerOrder();
        order.setUser(user);
        order.setReceiverName(request.getReceiverName());
        order.setDeliveryMobile(request.getMobile());
        order.setEmail(request.getEmail());
        order.setAddress(request.getAddress());
        order.setCity(request.getCity());
        order.setStateName(request.getStateName());
        order.setPincode(request.getPincode());
        order.setLandmark(request.getLandmark());
        order.setDeliveryInstructions(request.getDeliveryInstructions());
        order.setPaymentMethod(request.getPaymentMethod() == null ? "PhonePe Dummy" : request.getPaymentMethod());
        order.setPaymentStatus(request.getPaymentStatus() == null ? "PAID" : request.getPaymentStatus());
        order.setTransactionId(request.getTransactionId() == null ? "PAY-DEMO-" + System.currentTimeMillis() : request.getTransactionId());
        order.setStatus("ORDER_CONFIRMED");
        order.setTotalAmount(total);
        order.setDeliveryCharge(delivery);
        order.setDiscountAmount(discount);
        order.setCouponCode(couponCode);
        order.setPayableAmount(total.add(delivery).subtract(discount));
        order.setEstimatedDeliveryDate(LocalDateTime.now().plusDays(4));

        for (CartItem cartItem : cart) {
            Product product = cartItem.getProduct();
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setImageUrl(product.getImageUrl());
            item.setPrice(product.getPrice());
            item.setQuantity(cartItem.getQuantity());
            item.setSelectedSize(cartItem.getSelectedSize());
            order.getItems().add(item);
            product.setStock(Math.max(0, (product.getStock() == null ? 0 : product.getStock()) - cartItem.getQuantity()));
        }
        CustomerOrder saved = orderRepository.save(order);

        Payment payment = new Payment();
        payment.setOrder(saved);
        payment.setPaymentMethod(saved.getPaymentMethod());
        payment.setPaymentStatus(saved.getPaymentStatus());
        payment.setTransactionId(saved.getTransactionId());
        payment.setAmount(saved.getPayableAmount());
        paymentRepository.save(payment);

        createTracking(saved, "Order Confirmed", "SmartCart Warehouse", "Your order has been placed successfully.", 1);
        createTracking(saved, "Packed", "Warehouse", "Seller packed your products.", 2);
        createTracking(saved, "Shipped", "Nearest Hub", "Order is moving to your city hub.", 3);

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Order placed successfully");
        notification.setMessage("Order #" + saved.getId() + " placed. Track it from Orders page.");
        notification.setType("ORDER");
        notificationRepository.save(notification);

        cartItemRepository.deleteByUserIdAndSavedForLaterFalse(user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Order placed successfully", orderDto(saved, true)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> userOrders(HttpServletRequest request) {
        List<CustomerOrder> orders = orderRepository.findByUserIdOrderByOrderDateDesc(currentUser.get(request).getId());
        List<Map<String, Object>> data = new ArrayList<>();
        for (CustomerOrder order : orders) data.add(orderDto(order, true));
        return ResponseEntity.ok(ApiResponse.ok("Orders fetched", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> get(HttpServletRequest request, @PathVariable Long id) {
        CustomerOrder order = orderRepository.findByIdAndUserId(id, currentUser.get(request).getId()).orElse(null);
        if (order == null) return ResponseEntity.status(404).body(ApiResponse.fail("Order not found"));
        return ResponseEntity.ok(ApiResponse.ok("Order fetched", orderDto(order, true)));
    }

    @GetMapping("/{id}/tracking")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Map<String, Object>>> tracking(HttpServletRequest request, @PathVariable Long id) {
        CustomerOrder order = orderRepository.findByIdAndUserId(id, currentUser.get(request).getId()).orElse(null);
        if (order == null) return ResponseEntity.status(404).body(ApiResponse.fail("Order not found"));
        return ResponseEntity.ok(ApiResponse.ok("Tracking fetched", trackingPayload(order)));
    }

    @GetMapping("/tracking/search")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Map<String, Object>>> trackingSearch(HttpServletRequest request, @RequestParam Long orderId, @RequestParam String pincode) {
        User user = currentUser.get(request);
        CustomerOrder order = orderRepository.findByIdAndUserId(orderId, user.getId()).orElse(null);
        if (order == null || order.getPincode() == null || !order.getPincode().trim().equals(pincode.trim())) {
            return ResponseEntity.status(404).body(ApiResponse.fail("No order found for this order ID and PIN code"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Tracking fetched", trackingPayload(order)));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cancel(HttpServletRequest request, @PathVariable Long id) {
        CustomerOrder order = orderRepository.findByIdAndUserId(id, currentUser.get(request).getId()).orElse(null);
        if (order == null) return ResponseEntity.status(404).body(ApiResponse.fail("Order not found"));
        order.setStatus("CANCELLED");
        return ResponseEntity.ok(ApiResponse.ok("Order cancelled", orderDto(orderRepository.save(order), true)));
    }

    private Map<String, Object> trackingPayload(CustomerOrder order) {
        Map<String, Object> data = new HashMap<>();
        data.put("order", orderDto(order, false));
        List<Map<String, Object>> updates = new ArrayList<>();
        for (TrackingUpdate update : trackingUpdateRepository.findByOrderIdOrderByStepOrderAsc(order.getId())) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", update.getId());
            item.put("status", update.getStatus());
            item.put("location", update.getLocation());
            item.put("message", update.getMessage());
            item.put("stepOrder", update.getStepOrder());
            item.put("createdAt", update.getCreatedAt());
            updates.add(item);
        }
        if (updates.isEmpty()) {
            Map<String, Object> item = new HashMap<>();
            item.put("status", order.getStatus());
            item.put("location", "SmartCart Warehouse");
            item.put("message", "Tracking details will update soon.");
            item.put("stepOrder", 1);
            item.put("createdAt", order.getOrderDate());
            updates.add(item);
        }
        data.put("updates", updates);
        return data;
    }

    private Map<String, Object> orderDto(CustomerOrder order, boolean withItems) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", order.getId());
        data.put("totalAmount", order.getTotalAmount());
        data.put("discountAmount", order.getDiscountAmount());
        data.put("deliveryCharge", order.getDeliveryCharge());
        data.put("payableAmount", order.getPayableAmount());
        data.put("couponCode", order.getCouponCode());
        data.put("status", order.getStatus());
        data.put("receiverName", order.getReceiverName());
        data.put("deliveryMobile", order.getDeliveryMobile());
        data.put("email", order.getEmail());
        data.put("address", order.getAddress());
        data.put("city", order.getCity());
        data.put("stateName", order.getStateName());
        data.put("pincode", order.getPincode());
        data.put("landmark", order.getLandmark());
        data.put("deliveryInstructions", order.getDeliveryInstructions());
        data.put("paymentMethod", order.getPaymentMethod());
        data.put("paymentStatus", order.getPaymentStatus());
        data.put("transactionId", order.getTransactionId());
        data.put("estimatedDeliveryDate", order.getEstimatedDeliveryDate());
        data.put("orderDate", order.getOrderDate());
        if (order.getUser() != null) {
            Map<String, Object> user = new HashMap<>();
            user.put("id", order.getUser().getId());
            user.put("name", order.getUser().getName());
            user.put("email", order.getUser().getEmail());
            user.put("mobile", order.getUser().getMobile());
            data.put("user", user);
        }
        if (withItems) {
            List<Map<String, Object>> items = new ArrayList<>();
            for (OrderItem orderItem : order.getItems()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", orderItem.getId());
                item.put("productId", orderItem.getProductId());
                item.put("productName", orderItem.getProductName());
                item.put("imageUrl", orderItem.getImageUrl());
                item.put("price", orderItem.getPrice());
                item.put("quantity", orderItem.getQuantity());
                item.put("selectedSize", orderItem.getSelectedSize());
                items.add(item);
            }
            data.put("items", items);
        }
        return data;
    }

    private void createTracking(CustomerOrder order, String status, String location, String message, int step) {
        TrackingUpdate tracking = new TrackingUpdate();
        tracking.setOrder(order);
        tracking.setStatus(status);
        tracking.setLocation(location);
        tracking.setMessage(message);
        tracking.setStepOrder(step);
        trackingUpdateRepository.save(tracking);
    }
}
