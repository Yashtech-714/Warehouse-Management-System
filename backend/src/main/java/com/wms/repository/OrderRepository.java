package com.wms.repository;

import com.wms.model.core.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
	Optional<Order> findByOrderNumber(String orderNumber);

	List<Order> findByStatus(String status);
}
