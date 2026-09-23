# WMS Inventory Event Contracts

## 1. PutAwayCompleted

### Purpose

Published by Put-Away Service when a Put-Away task is successfully completed.

Inventory Service consumes this event and increases inventory quantity.

### Kafka Topic

putaway-completed

### Event Envelope

```json
{
  "eventId": "event-1001",
  "eventType": "PutAwayCompleted",
  "eventVersion": "1",
  "occurredAt": "2026-09-23T10:30:00",
  "source": "putaway-service",
  "correlationId": "corr-1001",
  "entityId": "PUT-1001",
  "payload": {
    "skuId": "SKU-1001",
    "warehouseId": "WH-001",
    "binId": "BIN-001",
    "quantity": 100,
    "referenceType": "PUTAWAY",
    "referenceId": "PUT-1001"
  }
}~~~~