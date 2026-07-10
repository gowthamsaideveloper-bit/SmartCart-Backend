package com.smartcart.repository;

import com.smartcart.model.TrackingUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TrackingUpdateRepository extends JpaRepository<TrackingUpdate, Long> {
    List<TrackingUpdate> findByOrderIdOrderByStepOrderAsc(Long orderId);
}
