package com.company.wms.inventory.controller;
import com.company.wms.inventory.dto.InventoryResponse;
import com.company.wms.inventory.service.InventoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{skuId}")
    public List<InventoryResponse> getInventory(
            @PathVariable String skuId) {

        return inventoryService.getInventoryBySku(skuId);
    }
}
