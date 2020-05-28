package com.yejianfengblue.sga.booking.inventory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryEventHandler {

    private final StreamBridge streamBridge;

    static final String INVENTORY_EVENT_OUT_BINDING = "inventory-out-0";

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    public void handleInventoryDomainEvent(InventoryEvent inventoryEvent) {

        streamBridge.send(INVENTORY_EVENT_OUT_BINDING, inventoryEvent);

        log.info("Send to inventory : {}", inventoryEvent);
    }
}
