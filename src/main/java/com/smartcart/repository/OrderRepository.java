package com.smartcart.repository;

import com.smartcart.model.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findByUserIdOrderByOrderDateDesc(Long userId);
    List<CustomerOrder> findAllByOrderByOrderDateDesc();
    Optional<CustomerOrder> findByIdAndUserId(Long id, Long userId);
    Optional<CustomerOrder> findByIdAndPincode(Long id, String pincode);
}
