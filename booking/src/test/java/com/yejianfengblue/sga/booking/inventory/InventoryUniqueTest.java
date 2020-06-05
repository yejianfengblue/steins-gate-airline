package com.yejianfengblue.sga.booking.inventory;

import com.yejianfengblue.sga.booking.common.ServiceType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataMongoTest
@Import({ValidationAutoConfiguration.class})
public class InventoryUniqueTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @AfterEach
    void cleanTestData() {
        inventoryRepository.deleteAll();
    }

    @Test
    void whenCreateInventoryWithSameCarrierAndFltNumAndFltDate_thenDuplicateKeyException() {

        LocalDate fltDate = LocalDate.of(2020, 1, 1);
        Inventory inventory = new Inventory("SG", "001", ServiceType.PAX,
                fltDate, fltDate.getDayOfWeek().getValue(),
                List.of(new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                        "HKG", "TPE", 1,
                        fltDate.atTime(10, 00), fltDate.atTime(16, 00), 480, 480,
                        100)));
        inventoryRepository.save(inventory);

        assertThatThrownBy(() ->
                inventoryRepository.save(new Inventory("SG", "001", ServiceType.PAX,
                        fltDate, fltDate.getDayOfWeek().getValue(),
                        List.of(new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                                "HKG", "TPE", 1,
                                fltDate.atTime(10, 00), fltDate.atTime(16, 00), 480, 480,
                                100)))))
                .isInstanceOf(DuplicateKeyException.class);

    }
}
