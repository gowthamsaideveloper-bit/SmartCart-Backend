package com.smartcart.repository;

import com.smartcart.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserId(Long userId);
    List<CartItem> findByUserIdAndSavedForLaterFalse(Long userId);
    List<CartItem> findByUserIdAndSavedForLaterTrue(Long userId);
    Optional<CartItem> findByUserIdAndProductIdAndSavedForLaterFalse(Long userId, Long productId);
    Optional<CartItem> findByUserIdAndProductIdAndSelectedSizeAndSavedForLaterFalse(Long userId, Long productId, String selectedSize);
    Optional<CartItem> findByIdAndUserId(Long id, Long userId);
    void deleteByUserId(Long userId);
    void deleteByUserIdAndSavedForLaterFalse(Long userId);
}
