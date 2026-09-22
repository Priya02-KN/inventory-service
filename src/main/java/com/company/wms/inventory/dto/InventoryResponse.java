package com.company.wms.inventory.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {

    private Long id;
    private String skuId;
    private String warehouseId;
    private String binId;
    private Long availableQuantity;
    private Long reservedQuantity;
}

