package com.yejianfengblue.sga.booking.inventory;

import com.yejianfengblue.sga.booking.common.ServiceType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;

import javax.validation.ConstraintViolationException;
import java.time.LocalDate;
import java.util.List;
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
        LocalDate fltDate = LocalDate.of(2020, 1, 1);
        String legDep = "HKG", legArr = "TPE";
        Inventory inventory = new Inventory("SG", "001", ServiceType.PAX,
                fltDate, fltDate.getDayOfWeek().getValue(),
                List.of(new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                        legDep, legArr, 1,
                        fltDate.atTime(10, 0), fltDate.atTime(16, 0), 480, 480,
                        99)));
        inventoryRepository.save(inventory);

        // when
        Optional<Inventory> foundInventory = inventoryService.findInventory("SG", "001", LocalDate.of(2020, 1, 1));

        // then
        assertThat(foundInventory).isPresent();
        assertThat(foundInventory.get().getCarrier()).isEqualTo("SG");
        assertThat(foundInventory.get().getFltNum()).isEqualTo("001");
        assertThat(foundInventory.get().getFltDate()).isEqualTo(LocalDate.of(2020, 1, 1));
        assertThat(foundInventory.get().getAvailable(legDep, legArr)).isEqualTo(99);
    }

    @Test
    void givenExistingInventory_whenSubtractInventory_thenInventoryAvailableIsReducedByGivenChange() {

        // given
        LocalDate fltDate = LocalDate.of(2020, 1, 1);
        String legDep = "HKG", legArr = "TPE";
        Inventory inventory = new Inventory("SG", "001", ServiceType.PAX,
                fltDate, fltDate.getDayOfWeek().getValue(),
                List.of(new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                        legDep, legArr, 1,
                        fltDate.atTime(10, 0), fltDate.atTime(16, 0), 480, 480,
                        99)));
        inventoryRepository.save(inventory);
        Optional<Inventory> foundInventory = inventoryService.findInventory("SG", "001", LocalDate.of(2020, 1, 1));
        assertThat(foundInventory).isPresent();

        // when
        Inventory inventoryAfterSubtract = inventoryService.subtractInventory(foundInventory.get(), legDep, legArr, 1);
        assertThat(inventoryAfterSubtract.getAvailable(legDep, legArr)).isEqualTo(98);

        Optional<Inventory> foundInventoryAfterSubtract = inventoryService.findInventory("SG", "001", LocalDate.of(2020, 1, 1));
        assertThat(foundInventoryAfterSubtract).isPresent();
        assertThat(foundInventoryAfterSubtract.get().getAvailable(legDep, legArr)).isEqualTo(98);
    }

    @Test
    void subtractInventoryChangeMustBePositive() {

        // given
        LocalDate fltDate = LocalDate.of(2020, 1, 1);
        String legDep = "HKG", legArr = "TPE";
        Inventory inventory = new Inventory("SG", "001", ServiceType.PAX,
                fltDate, fltDate.getDayOfWeek().getValue(),
                List.of(new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                        legDep, legArr, 1,
                        fltDate.atTime(10, 0), fltDate.atTime(16, 0), 480, 480,
                        99)));        inventoryRepository.save(inventory);
        Optional<Inventory> foundInventory = inventoryService.findInventory("SG", "001", LocalDate.of(2020, 1, 1));
        assertThat(foundInventory).isPresent();

        assertThatThrownBy(() ->
                inventoryService.subtractInventory(foundInventory.get(), legDep, legArr, -1)
        ).isInstanceOf(ConstraintViolationException.class)
                .hasMessage("subtractInventory.availableChange: must be greater than 0");

        assertThatThrownBy(() ->
                inventoryService.subtractInventory(foundInventory.get(), legDep, legArr, 0)
        ).isInstanceOf(ConstraintViolationException.class)
                .hasMessage("subtractInventory.availableChange: must be greater than 0");

        assertDoesNotThrow(() ->
                inventoryService.subtractInventory(foundInventory.get(), legDep, legArr, 1)
        );

    }

    @Test
    void givenExistingInventory_whenAddInventory_thenInventoryAvailableIsIncreasedByGivenChange() {

        // given
                LocalDate fltDate = LocalDate.of(2020, 1, 1);
        String legDep = "HKG", legArr = "TPE";
        Inventory inventory = new Inventory("SG", "001", ServiceType.PAX,
                fltDate, fltDate.getDayOfWeek().getValue(),
                List.of(new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                        legDep, legArr, 1,
                        fltDate.atTime(10, 0), fltDate.atTime(16, 0), 480, 480,
                        99)));
        inventoryRepository.save(inventory);
        Optional<Inventory> foundInventory = inventoryService.findInventory("SG", "001", LocalDate.of(2020, 1, 1));
        assertThat(foundInventory).isPresent();

        // when
        Inventory inventoryAfterAdd = inventoryService.addInventory(foundInventory.get(), legDep, legArr, 1);
        assertThat(inventoryAfterAdd.getAvailable(legDep, legArr)).isEqualTo(100);

        Optional<Inventory> foundInventoryAfterAdd = inventoryService.findInventory("SG", "001", LocalDate.of(2020, 1, 1));
        assertThat(foundInventoryAfterAdd).isPresent();
        assertThat(foundInventoryAfterAdd.get().getAvailable(legDep, legArr)).isEqualTo(100);
    }

    @Test
    void addInventoryChangeMinIs1() {

        // given
                LocalDate fltDate = LocalDate.of(2020, 1, 1);
        String legDep = "HKG", legArr = "TPE";
        Inventory inventory = new Inventory("SG", "001", ServiceType.PAX,
                fltDate, fltDate.getDayOfWeek().getValue(),
                List.of(new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                        legDep, legArr, 1,
                        fltDate.atTime(10, 0), fltDate.atTime(16, 0), 480, 480,
                        99)));
        inventoryRepository.save(inventory);
        Optional<Inventory> foundInventory = inventoryService.findInventory("SG", "001", LocalDate.of(2020, 1, 1));
        assertThat(foundInventory).isPresent();

        assertThatThrownBy(() ->
                inventoryService.addInventory(foundInventory.get(), legDep, legArr, -1)
        ).isInstanceOf(ConstraintViolationException.class)
                .hasMessage("addInventory.availableChange: must be greater than 0");

        assertThatThrownBy(() ->
                inventoryService.addInventory(foundInventory.get(), legDep, legArr, 0)
        ).isInstanceOf(ConstraintViolationException.class)
                .hasMessage("addInventory.availableChange: must be greater than 0");

        assertDoesNotThrow(() ->
                inventoryService.addInventory(foundInventory.get(), legDep, legArr, 1)
        );
    }

}
