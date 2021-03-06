package com.yejianfengblue.sga.search.inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
class InventoryEventHandler {

    private final InventoryRepository inventoryRepository;

    void handle(InventoryEvent inventoryEvent) {

        Inventory inventory = inventoryEvent.getInventory();

        Optional<Inventory> foundInventory = inventoryRepository.findByCarrierAndFltNumAndFltDate(
                inventory.getCarrier(),
                inventory.getFltNum(),
                inventory.getFltDate());

        foundInventory.ifPresent(inv -> inventory.setId(inv.getId()));
        inventoryRepository.save(inventory);
    }
}
