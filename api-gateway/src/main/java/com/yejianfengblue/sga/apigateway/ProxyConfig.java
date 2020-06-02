package com.yejianfengblue.sga.apigateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.security.oauth2.gateway.TokenRelayGatewayFilterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProxyConfig {

    @Bean
    RouteLocator routeLocator(RouteLocatorBuilder routeLocatorBuilder,
                              TokenRelayGatewayFilterFactory tokenRelayGatewayFilterFactory) {

        return routeLocatorBuilder.routes()
                .route("sga-flt-sch",
                        predicateSpec -> predicateSpec.path("/sga-flt-sch/**")
                                .filters(gatewayFilterSpec -> gatewayFilterSpec
                                        .stripPrefix(1)
                                        .filter(tokenRelayGatewayFilterFactory.apply()))
                                .uri("lb://sga-flt-sch"))
                .route("sga-booking",
                        predicateSpec -> predicateSpec.path("/sga-booking/**")
                                .filters(gatewayFilterSpec -> gatewayFilterSpec
                                        .stripPrefix(1)
                                        .filter(tokenRelayGatewayFilterFactory.apply()))
                                .uri("lb://sga-booking"))
                .route("sga-search",
                        predicateSpec -> predicateSpec.path("/sga-search/**")
                                .filters(gatewayFilterSpec -> gatewayFilterSpec
                                        .stripPrefix(1)
                                        .filter(tokenRelayGatewayFilterFactory.apply()))
                                .uri("lb://sga-search"))
                .build();
    }
}
