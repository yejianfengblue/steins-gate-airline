package com.yejianfengblue.sga.booking.inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
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

    /**
     * Subtract inventory available from the given segment.
     * If the segment doesn't exist, no change is made.
     * Segment existence can be checked by {@link Inventory#isValidSegment(String, String)}.
     * 
     * @throws IllegalStateException if the given {@link Inventory} is not persisted.
     */
    public Inventory subtractInventory(@NotNull Inventory inventory,
                                       @NotNull String segOrig,
                                       @NotNull String segDest,
                                       @Positive Integer availableChange) {

        if (inventory.getId() == null) {
            throw new IllegalStateException("Inventory ID must not be null");
        }
        
        inventory.subtractAvailable(segOrig, segDest, availableChange);
        return inventoryRepository.save(inventory);
    }

    /**
     * Add inventory available to the given segment.
     * If the segment doesn't exist, no change is made.
     * Segment existence can be checked by {@link Inventory#isValidSegment(String, String)}.
     *
     * @throws IllegalStateException if the given {@link Inventory} is not persisted.
     */
    public Inventory addInventory(@NotNull Inventory inventory,
                                  @NotNull String segOrig,
                                  @NotNull String segDest,
                                  @Positive Integer availableChange) {

        if (inventory.getId() == null) {
            throw new IllegalStateException("Inventory ID must not be null");
        }
        
        inventory.addAvailable(segOrig, segDest, availableChange);
        return inventoryRepository.save(inventory);
    }

    /**
     * Set inventory available to the given segment.
     * If the segment doesn't exist, no change is made.
     * Segment existence can be checked by {@link Inventory#isValidSegment(String, String)}.
     *
     * @throws IllegalStateException if the given {@link Inventory} is not persisted.
     */
    public Inventory setInventory(@NotNull Inventory inventory,
                                  @NotNull String segOrig,
                                  @NotNull String segDest,
                                  @Positive Integer available) {

        if (inventory.getId() == null) {
            throw new IllegalStateException("Inventory ID must not be null");
        }

        inventory.setAvailable(segOrig, segDest, available);
        return inventoryRepository.save(inventory);
    }
}
