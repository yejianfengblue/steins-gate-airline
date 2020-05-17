package com.yejianfengblue.sga.booking.config;

import com.fasterxml.jackson.databind.Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.jackson.datatype.money.MoneyModule;

@Configuration
class JacksonCustomization {

    @Bean
    public Module moneyModule() {
        return new MoneyModule();
    }

}