package com.yejianfengblue.sga.search.inventory;

import com.yejianfengblue.sga.search.common.ServiceType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataMongoTest
public class InventoryUniqueTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @AfterEach
    void deleteTestData() {
        inventoryRepository.deleteAll();
    }

    @Test
    void whenCreateInventoryWithSameCarrierAndFltNumAndFltDate_thenDuplicateKeyException() {

        LocalDate fltDate = LocalDate.of(2020, 1, 1);
        inventoryRepository.save(new Inventory("SG", "001", ServiceType.PAX,
                fltDate, fltDate.getDayOfWeek().getValue(),
                List.of(new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                        "HKG", "TPE", 1,
                        fltDate.atTime(10, 00), fltDate.atTime(14, 00), 480, 480,
                        100)),
                Instant.now(), Instant.now()));

        assertThatThrownBy(() ->
                inventoryRepository.save(new Inventory("SG", "001", ServiceType.FRTR,
                        fltDate, fltDate.getDayOfWeek().getValue(),
                        List.of(new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                                "XXX", "YYY", 2,
                                fltDate.atTime(16, 00), fltDate.atTime(20, 00), 0, 0,
                                99)),
                        Instant.now(), Instant.now())))
                .isInstanceOf(DuplicateKeyException.class);
    }
}
