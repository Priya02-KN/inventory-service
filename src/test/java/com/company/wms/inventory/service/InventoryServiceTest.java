package com.company.wms.inventory.service;
import com.company.wms.inventory.domain.InventoryBalance;
import com.company.wms.inventory.domain.InventoryMovement;
import com.company.wms.inventory.domain.OutboxEvent;
import com.company.wms.inventory.event.PutAwayCompletedEvent;
import com.company.wms.inventory.event.PutAwayPayload;
import com.company.wms.inventory.exception.InvalidInventoryException;
import com.company.wms.inventory.mapper.InventoryMapper;
import com.company.wms.inventory.repository.InventoryBalanceRepository;
import com.company.wms.inventory.repository.InventoryMovementRepository;
import com.company.wms.inventory.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InventoryServiceTest {

    private InventoryBalanceRepository inventoryBalanceRepository;
    private InventoryMovementRepository inventoryMovementRepository;
    private InventoryMapper inventoryMapper;
    private OutboxEventRepository outboxEventRepository;
    private ObjectMapper objectMapper;

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {

        inventoryBalanceRepository =
                mock(InventoryBalanceRepository.class);

        inventoryMovementRepository =
                mock(InventoryMovementRepository.class);

        inventoryMapper =
                mock(InventoryMapper.class);

        outboxEventRepository =
                mock(OutboxEventRepository.class);

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        inventoryService = new InventoryService(
                inventoryBalanceRepository,
                inventoryMovementRepository,
                inventoryMapper,
                outboxEventRepository,
                objectMapper
        );
    }

    // 1. Positive scenario
    @Test
    void processPutAway_shouldIncreaseInventoryAndCreateMovementAndOutbox() {

        PutAwayPayload payload = PutAwayPayload.builder()
                .skuId("SKU-1001")
                .warehouseId("WH-001")
                .binId("BIN-001")
                .quantity(5L)
                .referenceType("PUTAWAY")
                .referenceId("PUT-1001")
                .build();

        PutAwayCompletedEvent event =
                PutAwayCompletedEvent.builder()
                        .eventId("event-1001")
                        .eventType("PutAwayCompleted")
                        .eventVersion("1")
                        .source("putaway-service")
                        .correlationId("corr-1001")
                        .entityId("PUT-1001")
                        .payload(payload)
                        .build();

        InventoryBalance balance =
                InventoryBalance.builder()
                        .id(1L)
                        .skuId("SKU-1001")
                        .warehouseId("WH-001")
                        .binId("BIN-001")
                        .availableQuantity(10L)
                        .reservedQuantity(0L)
                        .build();

        when(inventoryMovementRepository
                .existsByEventId("event-1001"))
                .thenReturn(false);

        when(inventoryBalanceRepository
                .findBySkuIdAndWarehouseIdAndBinId(
                        "SKU-1001",
                        "WH-001",
                        "BIN-001"))
                .thenReturn(Optional.of(balance));

        when(inventoryBalanceRepository
                .save(any(InventoryBalance.class)))
                .thenReturn(balance);

        inventoryService.processPutAway(event);

        assertEquals(
                15L,
                balance.getAvailableQuantity()
        );

        verify(inventoryBalanceRepository)
                .save(balance);

        verify(inventoryMovementRepository)
                .save(any(InventoryMovement.class));

        verify(outboxEventRepository)
                .save(any(OutboxEvent.class));
    }

    // 2. Duplicate event / idempotency
    @Test
    void processPutAway_shouldIgnoreDuplicateEvent() {

        PutAwayPayload payload =
                PutAwayPayload.builder()
                        .skuId("SKU-1001")
                        .warehouseId("WH-001")
                        .binId("BIN-001")
                        .quantity(5L)
                        .referenceType("PUTAWAY")
                        .referenceId("PUT-1001")
                        .build();

        PutAwayCompletedEvent event =
                PutAwayCompletedEvent.builder()
                        .eventId("event-duplicate-1001")
                        .eventType("PutAwayCompleted")
                        .eventVersion("1")
                        .source("putaway-service")
                        .correlationId("corr-1001")
                        .entityId("PUT-1001")
                        .payload(payload)
                        .build();

        when(inventoryMovementRepository
                .existsByEventId("event-duplicate-1001"))
                .thenReturn(true);

        inventoryService.processPutAway(event);

        verify(inventoryMovementRepository)
                .existsByEventId("event-duplicate-1001");

        verify(inventoryBalanceRepository, never())
                .save(any(InventoryBalance.class));

        verify(inventoryMovementRepository, never())
                .save(any(InventoryMovement.class));

        verify(outboxEventRepository, never())
                .save(any(OutboxEvent.class));
    }

    // 3. Validation scenario
    @Test
    void processPutAway_shouldRejectInvalidQuantity() {

        PutAwayPayload payload =
                PutAwayPayload.builder()
                        .skuId("SKU-1001")
                        .warehouseId("WH-001")
                        .binId("BIN-001")
                        .quantity(0L)
                        .referenceType("PUTAWAY")
                        .referenceId("PUT-1001")
                        .build();

        PutAwayCompletedEvent event =
                PutAwayCompletedEvent.builder()
                        .eventId("event-invalid-1001")
                        .eventType("PutAwayCompleted")
                        .eventVersion("1")
                        .source("putaway-service")
                        .correlationId("corr-1001")
                        .entityId("PUT-1001")
                        .payload(payload)
                        .build();

        when(inventoryMovementRepository
                .existsByEventId("event-invalid-1001"))
                .thenReturn(false);

        InvalidInventoryException exception =
                assertThrows(
                        InvalidInventoryException.class,
                        () -> inventoryService.processPutAway(event)
                );

        assertEquals(
                "Quantity must be greater than zero",
                exception.getMessage()
        );

        verify(inventoryBalanceRepository, never())
                .save(any(InventoryBalance.class));

        verify(inventoryMovementRepository, never())
                .save(any(InventoryMovement.class));

        verify(outboxEventRepository, never())
                .save(any(OutboxEvent.class));
    }

    // 4. Failure / invalid data scenario
    @Test
    void processPutAway_shouldFailWhenSkuIdIsMissing() {

        PutAwayPayload payload =
                PutAwayPayload.builder()
                        .skuId("")
                        .warehouseId("WH-001")
                        .binId("BIN-001")
                        .quantity(5L)
                        .referenceType("PUTAWAY")
                        .referenceId("PUT-1001")
                        .build();

        PutAwayCompletedEvent event =
                PutAwayCompletedEvent.builder()
                        .eventId("event-failure-1001")
                        .eventType("PutAwayCompleted")
                        .eventVersion("1")
                        .source("putaway-service")
                        .correlationId("corr-1001")
                        .entityId("PUT-1001")
                        .payload(payload)
                        .build();

        when(inventoryMovementRepository
                .existsByEventId("event-failure-1001"))
                .thenReturn(false);

        InvalidInventoryException exception =
                assertThrows(
                        InvalidInventoryException.class,
                        () -> inventoryService.processPutAway(event)
                );

        assertEquals(
                "SKU ID is required",
                exception.getMessage()
        );

        verify(inventoryBalanceRepository, never())
                .save(any(InventoryBalance.class));

        verify(inventoryMovementRepository, never())
                .save(any(InventoryMovement.class));

        verify(outboxEventRepository, never())
                .save(any(OutboxEvent.class));
    }
}