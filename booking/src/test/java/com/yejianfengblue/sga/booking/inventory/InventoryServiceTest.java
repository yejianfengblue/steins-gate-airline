package com.yejianfengblue.sga.booking.inventory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;

import javax.validation.ConstraintViolationException;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DataMongoTest
@Import({InventoryService.class, ValidationAutoConfiguration.class})
public class InventoryServiceTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @AfterEach
    void cleanTestData() {
        inventoryRepository.deleteAll();
    }

    @Test
    void givenExistingInventory_whenGetInventoryByCarrierAndFltNumAndFltDate_thenReturnThatInventory() {

        // given
        Inventory inventory = new Inventory("SG", "001", LocalDate.of(2020, 1, 1), 99);
        inventoryRepository.save(inventory);

        // when
        Optional<Inventory> foundInventory = inventoryService.findInventory("SG", "001", LocalDate.of(2020, 1, 1));

        // then
        assertThat(foundInventory).isPresent();
        assertThat(foundInventory.get().getCarrier()).isEqualTo("SG");
        assertThat(foundInventory.get().getFltNum()).isEqualTo("001");
        assertThat(foundInventory.get().getFltDate()).isEqualTo(LocalDate.of(2020, 1, 1));
        assertThat(foundInventory.get().getAvailable()).isEqualTo(99);
    }

    @Test
    void givenExistingInventory_whenSubtractInventory_thenInventoryAvailableIsReducedByGivenChange() {

        // given
        Inventory inventory = new Inventory("SG", "001", LocalDate.of(2020, 1, 1), 99);
        inventoryRepository.save(inventory);
        Optional<Inventory> foundInventory = inventoryService.findInventory("SG", "001", LocalDate.of(2020, 1, 1));
        assertThat(foundInventory).isPresent();

        // when
        Inventory inventoryAfterSubtract = inventoryService.subtractInventory(foundInventory.get(), 1);
        assertThat(inventoryAfterSubtract.getAvailable()).isEqualTo(98);

        Optional<Inventory> foundInventoryAfterSubtract = inventoryService.findInventory("SG", "001", LocalDate.of(2020, 1, 1));
        assertThat(foundInventoryAfterSubtract).isPresent();
        assertThat(foundInventoryAfterSubtract.get().getAvailable()).isEqualTo(98);
    }

    @Test
    void subtractInventoryChangeMinIs1() {

        // given
        Inventory inventory = new Inventory("SG", "001", LocalDate.of(2020, 1, 1), 99);
        inventoryRepository.save(inventory);
        Optional<Inventory> foundInventory = inventoryService.findInventory("SG", "001", LocalDate.of(2020, 1, 1));
        assertThat(foundInventory).isPresent();

        assertThatThrownBy(() ->
                inventoryService.subtractInventory(foundInventory.get(), -1)
        ).isInstanceOf(ConstraintViolationException.class)
                .hasMessage("subtractInventory.change: must be greater than or equal to 1");

        assertThatThrownBy(() ->
                inventoryService.subtractInventory(foundInventory.get(), 0)
        ).isInstanceOf(ConstraintViolationException.class)
                .hasMessage("subtractInventory.change: must be greater than or equal to 1");

        assertDoesNotThrow(() ->
                inventoryService.subtractInventory(foundInventory.get(), 1)
        );

    }

    @Test
    void givenExistingInventory_whenAddInventory_thenInventoryAvailableIsIncreasedByGivenChange() {

        // given
        Inventory inventory = new Inventory("SG", "001", LocalDate.of(2020, 1, 1), 99);
        inventoryRepository.save(inventory);
        Optional<Inventory> foundInventory = inventoryService.findInventory("SG", "001", LocalDate.of(2020, 1, 1));
        assertThat(foundInventory).isPresent();

        // when
        Inventory inventoryAfterAdd = inventoryService.addInventory(foundInventory.get(), 1);
        assertThat(inventoryAfterAdd.getAvailable()).isEqualTo(100);

        Optional<Inventory> foundInventoryAfterAdd = inventoryService.findInventory("SG", "001", LocalDate.of(2020, 1, 1));
        assertThat(foundInventoryAfterAdd).isPresent();
        assertThat(foundInventoryAfterAdd.get().getAvailable()).isEqualTo(100);
    }

    @Test
    void addInventoryChangeMinIs1() {

        // given
        Inventory inventory = new Inventory("SG", "001", LocalDate.of(2020, 1, 1), 99);
        inventoryRepository.save(inventory);
        Optional<Inventory> foundInventory = inventoryService.findInventory("SG", "001", LocalDate.of(2020, 1, 1));
        assertThat(foundInventory).isPresent();

        assertThatThrownBy(() ->
                inventoryService.addInventory(foundInventory.get(), -1)
        ).isInstanceOf(ConstraintViolationException.class)
                .hasMessage("addInventory.change: must be greater than or equal to 1");

        assertThatThrownBy(() ->
                inventoryService.addInventory(foundInventory.get(), 0)
        ).isInstanceOf(ConstraintViolationException.class)
                .hasMessage("addInventory.change: must be greater than or equal to 1");

        assertDoesNotThrow(() ->
                inventoryService.addInventory(foundInventory.get(), 1)
        );
    }

}
