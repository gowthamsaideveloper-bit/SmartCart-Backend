package com.smartcart.controller;

import com.smartcart.config.CurrentUser;
import com.smartcart.dto.ApiResponse;
import com.smartcart.model.CartItem;
import com.smartcart.model.Product;
import com.smartcart.model.User;
import com.smartcart.repository.CartItemRepository;
import com.smartcart.repository.ProductRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CurrentUser currentUser;

    public CartController(CartItemRepository cartItemRepository, ProductRepository productRepository, CurrentUser currentUser) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.currentUser = currentUser;
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartItem>> add(HttpServletRequest request, @RequestParam Long productId, @RequestParam(defaultValue = "1") Integer quantity, @RequestParam(required = false) String size) {
        User user = currentUser.get(request);
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return ResponseEntity.badRequest().body(ApiResponse.fail("Invalid product"));
        if (product.getStock() == null || product.getStock() <= 0) return ResponseEntity.badRequest().body(ApiResponse.fail("Product is out of stock"));
        String selectedSize = size == null ? "" : size.trim();
        CartItem item = cartItemRepository.findByUserIdAndProductIdAndSelectedSizeAndSavedForLaterFalse(user.getId(), productId, selectedSize).orElse(new CartItem());
        item.setUser(user);
        item.setProduct(product);
        item.setSelectedSize(selectedSize);
        item.setSavedForLater(false);
        item.setQuantity((item.getQuantity() == null ? 0 : item.getQuantity()) + Math.max(quantity, 1));
        return ResponseEntity.ok(ApiResponse.ok("Product added to cart", cartItemRepository.save(item)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, List<CartItem>>>> getCart(HttpServletRequest request) {
        User user = currentUser.get(request);
        Map<String, List<CartItem>> data = new HashMap<>();
        data.put("cart", cartItemRepository.findByUserIdAndSavedForLaterFalse(user.getId()));
        data.put("savedForLater", cartItemRepository.findByUserIdAndSavedForLaterTrue(user.getId()));
        return ResponseEntity.ok(ApiResponse.ok("Cart fetched", data));
    }

    @PutMapping("/update/{itemId}")
    public ResponseEntity<ApiResponse<CartItem>> update(HttpServletRequest request, @PathVariable Long itemId, @RequestParam Integer quantity) {
        User user = currentUser.get(request);
        return cartItemRepository.findByIdAndUserId(itemId, user.getId())
                .map(item -> {
                    item.setQuantity(Math.max(quantity, 1));
                    return ResponseEntity.ok(ApiResponse.ok("Cart updated", cartItemRepository.save(item)));
                })
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.fail("Cart item not found")));
    }

    @PutMapping("/save-later/{itemId}")
    public ResponseEntity<ApiResponse<CartItem>> saveLater(HttpServletRequest request, @PathVariable Long itemId, @RequestParam(defaultValue = "true") Boolean saved) {
        User user = currentUser.get(request);
        return cartItemRepository.findByIdAndUserId(itemId, user.getId())
                .map(item -> {
                    item.setSavedForLater(saved);
                    return ResponseEntity.ok(ApiResponse.ok(saved ? "Saved for later" : "Moved to cart", cartItemRepository.save(item)));
                })
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.fail("Cart item not found")));
    }

    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<ApiResponse<String>> remove(HttpServletRequest request, @PathVariable Long itemId) {
        User user = currentUser.get(request);
        CartItem item = cartItemRepository.findByIdAndUserId(itemId, user.getId()).orElse(null);
        if (item == null) return ResponseEntity.status(404).body(ApiResponse.fail("Cart item not found"));
        cartItemRepository.delete(item);
        return ResponseEntity.ok(ApiResponse.ok("Cart item removed", "deleted"));
    }

    @DeleteMapping("/clear")
    @Transactional
    public ResponseEntity<ApiResponse<String>> clear(HttpServletRequest request) {
        cartItemRepository.deleteByUserIdAndSavedForLaterFalse(currentUser.get(request).getId());
        return ResponseEntity.ok(ApiResponse.ok("Cart cleared", "deleted"));
    }
}
