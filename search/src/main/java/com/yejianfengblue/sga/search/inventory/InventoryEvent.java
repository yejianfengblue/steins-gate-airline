package com.yejianfengblue.sga.search.inventory;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
class InventoryEvent {

    Inventory inventory;

    UUID id;

    Instant timestamp;

    InventoryEvent(@JsonProperty("inventory") Inventory inventory,
                   @JsonProperty("id") UUID id,
                   @JsonProperty("timestamp") Instant timestamp) {

        this.inventory = inventory;
        this.id = id;
        this.timestamp = timestamp;
    }
}
