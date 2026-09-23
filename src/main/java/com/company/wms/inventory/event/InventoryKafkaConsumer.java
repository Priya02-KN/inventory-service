package com.company.wms.inventory.event;
import com.company.wms.inventory.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryKafkaConsumer {

    private final InventoryService inventoryService;

    public InventoryKafkaConsumer(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(
            topics = "putaway-completed",
            groupId = "inventory-service-outbox-test"
    )
    public void consume(PutAwayCompletedEvent event) {

        System.out.println("===== PUTAWAY EVENT RECEIVED =====");
        System.out.println("Event ID: " + event.getEventId());
        System.out.println("SKU: " + event.getPayload().getSkuId());
        System.out.println("Quantity: " + event.getPayload().getQuantity());

        try {
            inventoryService.processPutAway(event);

            System.out.println("===== PUTAWAY EVENT PROCESSED =====");

        } catch (Exception e) {

            System.out.println("===== INVENTORY PROCESSING FAILED =====");
            e.printStackTrace();

            throw e;
        }
    }
}