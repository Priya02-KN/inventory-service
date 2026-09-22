package com.company.wms.inventory.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_balance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku_id", nullable = false)
    private String skuId;

    @Column(name = "warehouse_id", nullable = false)
    private String warehouseId;

    @Column(name = "bin_id", nullable = false)
    private String binId;

    @Column(name = "available_quantity", nullable = false)
    private Long availableQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private Long reservedQuantity;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        updatedAt = LocalDateTime.now();
    }
}

