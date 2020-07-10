package com.yejianfengblue.sga.booking;


import com.yejianfengblue.sga.booking.common.MoneyReadConverter;
import com.yejianfengblue.sga.booking.common.MoneyWriteConverter;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link MoneyReadConverter} and {@link MoneyWriteConverter}
 */
public class MoneyConverterTest {

    MoneyReadConverter moneyReadConverter = new MoneyReadConverter();
    
    MoneyWriteConverter moneyWriteConverter = new MoneyWriteConverter();

    @Test
    void handleNullValues() {

        assertThat(moneyWriteConverter.convert(null))
                .isNull();
        assertThat(moneyReadConverter.convert(null))
                .isNull();
    }

    @Test
    void handleSimpleValue() {

        assertThat(moneyWriteConverter.convert(Money.of(1.23, "HKD")))
                .isEqualTo("HKD 1.23");
        assertThat(moneyReadConverter.convert("HKD 1.23"))
                .isEqualTo(Money.of(1.23, "HKD"));
    }

    @Test
    void handleNegativeValue() {

        assertThat(moneyWriteConverter.convert(Money.of(-4.50, "HKD")))
                .isEqualTo("HKD -4.5");
        assertThat(moneyReadConverter.convert("HKD -4.5"))
                .isEqualTo(Money.of(-4.50, "HKD"));
    }

    @Test
    void doNotRoundValue() {

        assertThat(moneyWriteConverter.convert(Money.of(1.23456789, "HKD")))
                .isEqualTo("HKD 1.23456789");
    }

    @Test
    void doNotFormatLargeValue() {

        assertThat(moneyWriteConverter.convert(Money.of(123456789, "HKD")))
                .isEqualTo("HKD 123456789");
    }

    @Test
    void deserializeFormattedValue() {

        assertThat(moneyReadConverter.convert("HKD 123,456.789"))
                .isEqualTo(Money.of(123456.789, "HKD"));
    }
}
