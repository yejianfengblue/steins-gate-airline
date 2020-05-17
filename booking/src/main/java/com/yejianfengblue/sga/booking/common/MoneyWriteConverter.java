package com.yejianfengblue.sga.booking.common;

import org.javamoney.moneta.Money;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class MoneyWriteConverter implements Converter<Money, String> {

    @Override
    public String convert(Money money) {
        return null == money ? null : money.toString();
    }
}