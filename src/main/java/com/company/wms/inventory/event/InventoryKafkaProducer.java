package com.company.wms.inventory.event;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryKafkaProducer {

    private final KafkaTemplate<String, InventoryUpdatedEvent> kafkaTemplate;

    public InventoryKafkaProducer(
            KafkaTemplate<String, InventoryUpdatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishInventoryUpdated(InventoryUpdatedEvent event) {

        kafkaTemplate.send(
                "inventory-updated",
                event.getPayload().getSkuId(),
                event
        );

        System.out.println(
                "InventoryUpdated event published for SKU: "
                        + event.getPayload().getSkuId()
        );
    }
}

