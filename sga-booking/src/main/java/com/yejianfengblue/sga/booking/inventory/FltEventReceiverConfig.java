package com.yejianfengblue.sga.booking.inventory;

import com.yejianfengblue.sga.booking.inventory.FltEvent.Flt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;
import java.util.function.Consumer;

@Configuration
@Slf4j
public class FltEventReceiverConfig {

    @Bean
    public Consumer<FltEvent> fltEventReceiver(InventoryRepository inventoryRepository) {

        return fltEvent -> {

            log.info("Receive FltEvent of type {} : {}", fltEvent.getType(), fltEvent);

            // currently only handle CREATE flt event
            if (FltEvent.Type.CREATE.equals(fltEvent.getType())) {

                Flt flt = fltEvent.getFlt();
                Optional<Inventory> foundInventory = inventoryRepository.findByCarrierAndFltNumAndFltDate(
                        flt.getCarrier(), flt.getFltNum(), flt.getFltDate());
                if (foundInventory.isEmpty()) {

                    // In reality, there should be an application which manages the initial available value
                    Inventory inventory = new Inventory(flt, 100);
                    inventoryRepository.save(inventory);

                } else {
                    // TODO what if the flight already exists?
                }
            }
        };
    }
}
