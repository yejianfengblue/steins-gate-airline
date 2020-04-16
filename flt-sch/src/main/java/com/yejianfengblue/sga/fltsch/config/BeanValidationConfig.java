package com.yejianfengblue.sga.fltsch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class BeanValidationConfig {

    @Bean
    public LocalValidatorFactoryBean validator() {

        return new LocalValidatorFactoryBean();
    }
}
