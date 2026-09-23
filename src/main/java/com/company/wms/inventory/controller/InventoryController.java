package com.company.wms.inventory.controller;
import com.company.wms.inventory.dto.InventoryResponse;
import com.company.wms.inventory.event.PutAwayCompletedEvent;
import com.company.wms.inventory.event.PutAwayPayload;
import com.company.wms.inventory.service.InventoryService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    @PostMapping("/test-update")
    public String testInventoryUpdate() {

        PutAwayCompletedEvent event =
                PutAwayCompletedEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .eventType("PutAwayCompleted")
                        .eventVersion("1")
                        .occurredAt(LocalDateTime.now())
                        .source("test")
                        .correlationId("test-correlation")
                        .entityId("test-putaway")
                        .payload(
                                PutAwayPayload.builder()
                                        .skuId("SKU-1001")
                                        .warehouseId("WH-001")
                                        .binId("BIN-001")
                                        .quantity(5L)
                                        .referenceType("PUTAWAY")
                                        .referenceId("test-putaway")
                                        .build()
                        )
                        .build();

        inventoryService.processPutAway(event);

        return "Test inventory update triggered";
    }
}