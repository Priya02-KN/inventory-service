package com.company.wms.inventory.service;
import com.company.wms.inventory.domain.OutboxEvent;
import com.company.wms.inventory.event.InventoryUpdatedEvent;
import com.company.wms.inventory.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, InventoryUpdatedEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, InventoryUpdatedEvent> kafkaTemplate,
            ObjectMapper objectMapper) {

        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 300000)
    public void publishOutboxEvents() {

        List<OutboxEvent> events =
                outboxEventRepository.findByPublishedFalse();

        for (OutboxEvent outboxEvent : events) {

            try {

                InventoryUpdatedEvent event =
                        objectMapper.readValue(
                                outboxEvent.getPayload(),
                                InventoryUpdatedEvent.class
                        );

                kafkaTemplate.send(
                        outboxEvent.getTopic(),
                        outboxEvent.getEventKey(),
                        event
                ).whenComplete((result, exception) -> {

                    if (exception == null) {

                        outboxEvent.setPublished(true);
                        outboxEventRepository.save(outboxEvent);

                        System.out.println(
                                "Outbox event published to Kafka: "
                                        + outboxEvent.getEventId()
                        );

                    } else {

                        System.out.println(
                                "Failed to publish Outbox event: "
                                        + outboxEvent.getEventId()
                        );
                    }
                });

            } catch (Exception e) {

                System.out.println(
                        "Error processing Outbox event: "
                                + outboxEvent.getEventId()
                );

                e.printStackTrace();
            }
        }
    }
}

