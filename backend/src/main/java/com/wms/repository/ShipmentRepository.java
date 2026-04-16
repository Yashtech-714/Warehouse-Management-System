package com.wms.repository;

import com.wms.model.core.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
	Optional<Shipment> findByShipmentNumber(String shipmentNumber);

	List<Shipment> findByStatus(String status);
}
