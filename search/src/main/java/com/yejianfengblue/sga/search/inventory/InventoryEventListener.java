package com.yejianfengblue.sga.search.inventory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryEventListener {

    public static final String TOPIC = "inventory-out-0";

    private final InventoryEventHandler inventoryEventHandler;

    @KafkaListener(id = "inventory-search-group",
            topics = TOPIC,
            // set default type because kafka header "__TypeId__" doesn't exist in the message sent by Spring Cloud Stream
            properties = {
                    "spring.json.value.default.type : com.yejianfengblue.sga.search.inventory.InventoryEvent"
            })
    void listen(InventoryEvent inventoryEvent) {

        log.info("Receive from topic '{}' : {}", TOPIC, inventoryEvent);

        inventoryEventHandler.handle(inventoryEvent);
    }
}
