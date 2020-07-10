package com.yejianfengblue.sga.booking.config;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.core.mapping.ExposureConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.validation.Validator;

@Configuration
@RequiredArgsConstructor
public class RepositoryRestConfig implements RepositoryRestConfigurer {

    private final Validator beanValidator;

    @Override
    public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingListener) {

        // enable bean validation before create and save (update)
        validatingListener.addValidator("beforeCreate", beanValidator);
        validatingListener.addValidator("beforeSave", beanValidator);
    }

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration repositoryRestConfiguration) {

        ExposureConfiguration exposureConfiguration = repositoryRestConfiguration.getExposureConfiguration();
        exposureConfiguration.disablePutForCreation();
        exposureConfiguration.disablePutOnItemResources();
    }
}