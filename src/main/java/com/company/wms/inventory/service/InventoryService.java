package com.company.wms.inventory.service;
import com.company.wms.inventory.domain.InventoryBalance;
import com.company.wms.inventory.domain.InventoryMovement;
import com.company.wms.inventory.domain.OutboxEvent;
import com.company.wms.inventory.dto.InventoryResponse;
import com.company.wms.inventory.event.InventoryUpdatedEvent;
import com.company.wms.inventory.event.PutAwayCompletedEvent;
import com.company.wms.inventory.mapper.InventoryMapper;
import com.company.wms.inventory.repository.InventoryBalanceRepository;
import com.company.wms.inventory.repository.InventoryMovementRepository;
import com.company.wms.inventory.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryService {

    private final InventoryBalanceRepository inventoryBalanceRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryMapper inventoryMapper;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public InventoryService(
            InventoryBalanceRepository inventoryBalanceRepository,
            InventoryMovementRepository inventoryMovementRepository,
            InventoryMapper inventoryMapper,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper) {

        this.inventoryBalanceRepository = inventoryBalanceRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.inventoryMapper = inventoryMapper;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    public List<InventoryResponse> getInventoryBySku(String skuId) {

        List<InventoryBalance> inventoryBalances =
                inventoryBalanceRepository.findBySkuId(skuId);

        return inventoryBalances.stream()
                .map(inventoryMapper::toResponse)
                .toList();
    }

    @Transactional
    public void processPutAway(PutAwayCompletedEvent event) {

        // 1. Check duplicate event
        if (inventoryMovementRepository.existsByEventId(event.getEventId())) {

            System.out.println(
                    "Duplicate event ignored: " + event.getEventId()
            );

            return;
        }

        // 2. Validate quantity
        if (event.getQuantity() == null || event.getQuantity() <= 0) {

            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        // 3. Find existing inventory balance
        InventoryBalance inventoryBalance =
                inventoryBalanceRepository
                        .findBySkuIdAndWarehouseIdAndBinId(
                                event.getSkuId(),
                                event.getWarehouseId(),
                                event.getBinId()
                        )
                        .orElseGet(() -> InventoryBalance.builder()
                                .skuId(event.getSkuId())
                                .warehouseId(event.getWarehouseId())
                                .binId(event.getBinId())
                                .availableQuantity(0L)
                                .reservedQuantity(0L)
                                .build());

        // 4. Increase available quantity
        inventoryBalance.setAvailableQuantity(
                inventoryBalance.getAvailableQuantity()
                        + event.getQuantity()
        );

        inventoryBalanceRepository.save(inventoryBalance);

        // 5. Create inventory movement
        InventoryMovement movement = InventoryMovement.builder()
                .skuId(event.getSkuId())
                .warehouseId(event.getWarehouseId())
                .binId(event.getBinId())
                .movementType("PUT_AWAY")
                .quantity(event.getQuantity())
                .referenceType(event.getReferenceType())
                .referenceId(event.getReferenceId())
                .eventId(event.getEventId())
                .build();

        inventoryMovementRepository.save(movement);

        // 6. Create InventoryUpdated event
        InventoryUpdatedEvent updatedEvent =
                InventoryUpdatedEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .eventType("InventoryUpdated")
                        .eventVersion("1")
                        .occurredAt(LocalDateTime.now())
                        .source("inventory-service")
                        .correlationId(event.getCorrelationId())
                        .entityId(event.getEntityId())
                        .skuId(event.getSkuId())
                        .warehouseId(event.getWarehouseId())
                        .binId(event.getBinId())
                        .availableQuantity(
                                inventoryBalance.getAvailableQuantity()
                        )
                        .build();

        // 7. Save InventoryUpdated event to Outbox
        try {

            String payload =
                    objectMapper.writeValueAsString(updatedEvent);

            OutboxEvent outboxEvent =
                    OutboxEvent.builder()
                            .eventId(updatedEvent.getEventId())
                            .eventType("InventoryUpdated")
                            .topic("inventory-updated")
                            .eventKey(updatedEvent.getSkuId())
                            .payload(payload)
                            .published(false)
                            .build();

            outboxEventRepository.save(outboxEvent);

            System.out.println(
                    "InventoryUpdated event saved to Outbox for SKU: "
                            + updatedEvent.getSkuId()
            );

        } catch (JsonProcessingException e) {

            throw new RuntimeException(
                    "Failed to create outbox event", e
            );
        }

        // 8. Log success
        System.out.println(
                "Inventory updated successfully for SKU: "
                        + event.getSkuId()
        );
    }
}