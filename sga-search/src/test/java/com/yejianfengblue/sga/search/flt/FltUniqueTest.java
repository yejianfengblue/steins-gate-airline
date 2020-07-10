package com.yejianfengblue.sga.search.flt;

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
public class FltUniqueTest {

    @Autowired
    private FltRepository fltRepository;

    @AfterEach
    void deleteTestData() {
        fltRepository.deleteAll();
    }

    @Test
    void whenCreateFltWithSameCarrierAndFltNumAndFltDate_thenDuplicateKeyException() {

        LocalDate fltDate = LocalDate.of(2020, 1, 1);
        fltRepository.save(new Flt("SG", "001", ServiceType.PAX, fltDate, fltDate.getDayOfWeek().getValue(),
                List.of(
                        new FltLeg(fltDate, fltDate.getDayOfWeek().getValue(), "HKG", "TPE", 1,
                                fltDate.atTime(0, 0), fltDate.atTime(4, 0),
                                fltDate.atTime(0, 0), fltDate.atTime(4, 0),
                                fltDate.atTime(0, 0), fltDate.atTime(4, 0),
                                480, 480, "B-LAD", "333")),
                "Tester", Instant.now(), "Tester", Instant.now()));

        assertThatThrownBy(() ->
                fltRepository.save(new Flt("SG", "001", ServiceType.FRTR, fltDate, fltDate.getDayOfWeek().getValue(),
                        List.of(
                                new FltLeg(fltDate, fltDate.getDayOfWeek().getValue(), "XXX", "YYY", 2,
                                        fltDate.atTime(10, 0), fltDate.atTime(14, 0),
                                        fltDate.atTime(10, 0), fltDate.atTime(14, 0),
                                        fltDate.atTime(10, 0), fltDate.atTime(14, 0),
                                        0, 0, "B-XYZ", "999")),
                        "X", Instant.now(), "X", Instant.now())))
                .isInstanceOf(DuplicateKeyException.class);
    }
}
