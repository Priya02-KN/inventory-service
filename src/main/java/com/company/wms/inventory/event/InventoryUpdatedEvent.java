package com.company.wms.inventory.event;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryUpdatedEvent {

    private String eventId;
    private String eventType;
    private String eventVersion;
    private LocalDateTime occurredAt;
    private String source;
    private String correlationId;
    private String entityId;

    private String skuId;
    private String warehouseId;
    private String binId;
    private Long availableQuantity;
}

