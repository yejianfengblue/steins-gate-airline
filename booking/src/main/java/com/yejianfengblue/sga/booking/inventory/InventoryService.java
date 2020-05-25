package com.yejianfengblue.sga.booking.inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.util.Optional;

@Service
@Validated
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public Optional<Inventory> findInventory(String carrier, String fltNum, LocalDate fltDate) {

        return inventoryRepository.findByCarrierAndFltNumAndFltDate(carrier, fltNum, fltDate);
    }

    public Inventory subtractInventory(Inventory inventory, @Min(1) Integer change) {

        inventory.subtract(change);
        return inventoryRepository.save(inventory);
    }

    public Inventory addInventory(Inventory inventory, @Min(1) Integer change) {

        inventory.add(change);
        return inventoryRepository.save(inventory);
    }
}
