package com.yejianfengblue.sga.fltsch.config;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
@RequiredArgsConstructor
public class RepositoryRestConfig implements RepositoryRestConfigurer {

    @NonNull
    private final LocalValidatorFactoryBean beanValidator;

    @Override
    public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingListener) {

        // enable bean validation before create and save (update)
        validatingListener.addValidator("beforeCreate", beanValidator);
        validatingListener.addValidator("beforeSave", beanValidator);
    }
}