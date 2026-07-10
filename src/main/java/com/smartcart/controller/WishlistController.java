package com.smartcart.controller;

import com.smartcart.config.CurrentUser;
import com.smartcart.dto.ApiResponse;
import com.smartcart.model.Product;
import com.smartcart.model.User;
import com.smartcart.model.WishlistItem;
import com.smartcart.repository.ProductRepository;
import com.smartcart.repository.WishlistRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {
    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final CurrentUser currentUser;

    public WishlistController(WishlistRepository wishlistRepository, ProductRepository productRepository, CurrentUser currentUser) {
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
        this.currentUser = currentUser;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WishlistItem>>> all(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Wishlist fetched", wishlistRepository.findByUserIdOrderByAddedAtDesc(currentUser.get(request).getId())));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<WishlistItem>> add(HttpServletRequest request, @RequestParam Long productId) {
        User user = currentUser.get(request);
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return ResponseEntity.badRequest().body(ApiResponse.fail("Product not found"));
        WishlistItem item = wishlistRepository.findByUserIdAndProductId(user.getId(), productId).orElse(new WishlistItem());
        item.setUser(user);
        item.setProduct(product);
        return ResponseEntity.ok(ApiResponse.ok("Product added to wishlist", wishlistRepository.save(item)));
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<ApiResponse<String>> remove(HttpServletRequest request, @PathVariable Long id) {
        User user = currentUser.get(request);
        WishlistItem item = wishlistRepository.findByIdAndUserId(id, user.getId()).orElse(null);
        if (item == null) return ResponseEntity.status(404).body(ApiResponse.fail("Wishlist item not found"));
        wishlistRepository.delete(item);
        return ResponseEntity.ok(ApiResponse.ok("Wishlist item removed", "deleted"));
    }
}
