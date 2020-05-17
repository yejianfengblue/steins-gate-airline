package com.yejianfengblue.sga.fltsch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@EnableMongoAuditing
@Configuration
public class AuditConfig {

    @Component
    public static class SpringSecurityAuditorAware implements AuditorAware<String>{

        @Override
        public Optional<String> getCurrentAuditor() {

            return Optional.of(
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication()
                            .getName());
        }

    }

}
