package com.smartcart.controller;

import com.smartcart.config.CurrentUser;
import com.smartcart.dto.ApiResponse;
import com.smartcart.dto.ProductRequest;
import com.smartcart.model.Category;
import com.smartcart.model.CustomerOrder;
import com.smartcart.model.Product;
import com.smartcart.model.User;
import com.smartcart.repository.CategoryRepository;
import com.smartcart.repository.OrderRepository;
import com.smartcart.repository.ProductRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CurrentUser currentUser;

    public AdminController(CategoryRepository categoryRepository, ProductRepository productRepository, OrderRepository orderRepository, CurrentUser currentUser) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.currentUser = currentUser;
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<Category>> createCategory(HttpServletRequest request, @RequestBody Category category) {
        if (!isAdmin(request)) return ResponseEntity.status(403).body(ApiResponse.fail("Admin access only"));
        return ResponseEntity.ok(ApiResponse.ok("Category saved", categoryRepository.save(category)));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<Product>>> products(HttpServletRequest request) {
        if (!isAdmin(request)) return ResponseEntity.status(403).body(ApiResponse.fail("Admin access only"));
        return ResponseEntity.ok(ApiResponse.ok("Products fetched", productRepository.findAll()));
    }

    @PostMapping("/products")
    public ResponseEntity<ApiResponse<Product>> createProduct(HttpServletRequest request, @RequestBody ProductRequest productRequest) {
        if (!isAdmin(request)) return ResponseEntity.status(403).body(ApiResponse.fail("Admin access only"));
        Category category = categoryRepository.findById(productRequest.getCategoryId()).orElse(null);
        if (category == null) return ResponseEntity.badRequest().body(ApiResponse.fail("Invalid category"));
        Product product = new Product();
        applyProduct(product, productRequest, category);
        return ResponseEntity.ok(ApiResponse.ok("Product saved", productRepository.save(product)));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(HttpServletRequest request, @PathVariable Long id, @RequestBody ProductRequest productRequest) {
        if (!isAdmin(request)) return ResponseEntity.status(403).body(ApiResponse.fail("Admin access only"));
        Product product = productRepository.findById(id).orElse(null);
        Category category = categoryRepository.findById(productRequest.getCategoryId()).orElse(null);
        if (product == null || category == null) return ResponseEntity.status(404).body(ApiResponse.fail("Product or category not found"));
        applyProduct(product, productRequest, category);
        return ResponseEntity.ok(ApiResponse.ok("Product updated", productRepository.save(product)));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(HttpServletRequest request, @PathVariable Long id) {
        if (!isAdmin(request)) return ResponseEntity.status(403).body(ApiResponse.fail("Admin access only"));
        if (!productRepository.existsById(id)) return ResponseEntity.status(404).body(ApiResponse.fail("Product not found"));
        productRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Product deleted", "deleted"));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> orders(HttpServletRequest request) {
        if (!isAdmin(request)) return ResponseEntity.status(403).body(ApiResponse.fail("Admin access only"));
        List<Map<String, Object>> data = new ArrayList<>();
        for (CustomerOrder order : orderRepository.findAllByOrderByOrderDateDesc()) data.add(orderAdminDto(order));
        return ResponseEntity.ok(ApiResponse.ok("Orders fetched", data));
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateOrderStatus(HttpServletRequest request, @PathVariable Long id, @RequestParam String status) {
        if (!isAdmin(request)) return ResponseEntity.status(403).body(ApiResponse.fail("Admin access only"));
        CustomerOrder order = orderRepository.findById(id).orElse(null);
        if (order == null) return ResponseEntity.status(404).body(ApiResponse.fail("Order not found"));
        order.setStatus(status.toUpperCase());
        return ResponseEntity.ok(ApiResponse.ok("Order status updated", orderAdminDto(orderRepository.save(order))));
    }

    private boolean isAdmin(HttpServletRequest request) {
        User user = currentUser.get(request);
        return user.getRole() != null && user.getRole().equalsIgnoreCase("ADMIN");
    }

    private Map<String, Object> orderAdminDto(CustomerOrder order) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", order.getId());
        data.put("totalAmount", order.getTotalAmount());
        data.put("payableAmount", order.getPayableAmount());
        data.put("status", order.getStatus());
        data.put("orderDate", order.getOrderDate());
        if (order.getUser() != null) {
            Map<String, Object> user = new HashMap<>();
            user.put("id", order.getUser().getId());
            user.put("name", order.getUser().getName());
            user.put("email", order.getUser().getEmail());
            data.put("user", user);
        }
        return data;
    }

    private void applyProduct(Product product, ProductRequest r, Category category) {
        product.setName(r.getName());
        product.setDescription(r.getDescription());
        product.setPrice(r.getPrice());
        product.setMrp(r.getMrp());
        product.setDiscountPercent(r.getDiscountPercent());
        product.setStock(r.getStock());
        product.setImageUrl(r.getImageUrl());
        product.setCategory(category);
        product.setBrand(r.getBrand());
        product.setSizeChart(r.getSizeChart());
        product.setColor(r.getColor());
        product.setRating(r.getRating());
        product.setReviewCount(r.getReviewCount());
        product.setStyleTag(r.getStyleTag());
        product.setDemandTag(r.getDemandTag());
        product.setSeasonTag(r.getSeasonTag());
        product.setProductType(r.getProductType());
        product.setAvailablePincodes(r.getAvailablePincodes());
        product.setDeliveryCharge(r.getDeliveryCharge());
        product.setEstimatedDeliveryDays(r.getEstimatedDeliveryDays());
        product.setTrending(r.getTrending() != null && r.getTrending());
        product.setFeatured(r.getFeatured() != null && r.getFeatured());
    }
}
