package com.company.wms.inventory.domain;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_movement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku_id", nullable = false)
    private String skuId;

    @Column(name = "warehouse_id", nullable = false)
    private String warehouseId;

    @Column(name = "bin_id", nullable = false)
    private String binId;

    @Column(name = "movement_type", nullable = false)
    private String movementType;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Column(name = "reference_type")
    private String referenceType;

    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

