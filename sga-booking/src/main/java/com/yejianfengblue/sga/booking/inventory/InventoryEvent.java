package com.yejianfengblue.sga.booking.inventory;

import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event of {@link Inventory}
 */
@Value
class InventoryEvent {

    Inventory inventory;

    UUID id;

    Instant timestamp;

    static InventoryEvent of(Inventory inventory) {
        return new InventoryEvent(inventory, UUID.randomUUID(), Instant.now());
    }

    private InventoryEvent(Inventory inventory, UUID id, Instant timestamp) {

        this.inventory = inventory;
        this.id = id;
        this.timestamp = timestamp;
    }
}
