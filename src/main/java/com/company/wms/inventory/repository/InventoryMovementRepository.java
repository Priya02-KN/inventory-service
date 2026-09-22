package com.company.wms.inventory.repository;
import com.company.wms.inventory.domain.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
public interface InventoryMovementRepository
        extends JpaRepository<InventoryMovement, Long> {

    boolean existsByEventId(String eventId);
}

