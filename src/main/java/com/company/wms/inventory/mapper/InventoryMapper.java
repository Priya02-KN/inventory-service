package com.company.wms.inventory.mapper;

import com.company.wms.inventory.domain.InventoryBalance;
import com.company.wms.inventory.dto.InventoryResponse;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    public InventoryResponse toResponse(InventoryBalance inventoryBalance) {

        return InventoryResponse.builder()
                .id(inventoryBalance.getId())
                .skuId(inventoryBalance.getSkuId())
                .warehouseId(inventoryBalance.getWarehouseId())
                .binId(inventoryBalance.getBinId())
                .availableQuantity(inventoryBalance.getAvailableQuantity())
                .reservedQuantity(inventoryBalance.getReservedQuantity())
                .build();
    }
}

