package com.company.wms.inventory.event;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryPayload {

    private String skuId;
    private String warehouseId;
    private String binId;
    private Long availableQuantity;
}

