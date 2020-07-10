package com.yejianfengblue.sga.booking.common;

import javax.money.CurrencyUnit;
import javax.money.Monetary;

public interface Currencies {

    CurrencyUnit CNY = Monetary.getCurrency("CNY");

    CurrencyUnit EUR = Monetary.getCurrency("EUR");

    CurrencyUnit HKD = Monetary.getCurrency("HKD");

    CurrencyUnit USD = Monetary.getCurrency("USD");
}