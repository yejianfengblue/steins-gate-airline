package com.yejianfengblue.sga.booking.common;

import org.javamoney.moneta.Money;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import java.util.Locale;

@ReadingConverter
public class MoneyReadConverter implements Converter<String, Money> {

    private static final MonetaryAmountFormat FORMAT = MonetaryFormats.getAmountFormat(Locale.ROOT);

    @Override
    public Money convert(String source) {
        return null == source ? null : Money.parse(source, FORMAT);
    }
}