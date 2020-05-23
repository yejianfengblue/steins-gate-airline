package com.yejianfengblue.sga.booking.flt;

import com.yejianfengblue.sga.booking.inventory.Inventory;
import com.yejianfengblue.sga.booking.inventory.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;
import java.util.function.Consumer;

@Configuration
@Slf4j
public class FltEventReceiverConfig {

    @Bean
    public Consumer<FltEvent> fltEventReceiver(FltRepository fltRepository,
                                         InventoryRepository inventoryRepository) {

        return fltEvent -> {

            log.info("Receive FltEvent of type {} : {}", fltEvent.getType(), fltEvent);

            // currently only handle CREATE flt event
            if (FltEvent.Type.CREATE.equals(fltEvent.getType())) {

                Flt flt = fltEvent.getFlt();
                Optional<Flt> foundFlt = fltRepository.findByCarrierAndFltNumAndFltDate(
                        flt.getCarrier(), flt.getFltNum(), flt.getFltDate());
                if (foundFlt.isEmpty()) {

                    fltRepository.save(flt);

                    // In reality, there should be an application which manages the initial available value
                    Inventory inventory = new Inventory(flt.getCarrier(), flt.getFltNum(), flt.getFltDate(), 100);
                    inventoryRepository.save(inventory);

                } else {
                    // TODO what if the flight already exists?
                }
            }
        };
    }
}
