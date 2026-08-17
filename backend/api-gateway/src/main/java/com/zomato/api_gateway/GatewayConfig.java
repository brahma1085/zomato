package com.zomato.api_gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.web.servlet.function.RequestPredicates.path;
import static org.springframework.cloud.gateway.server.mvc.filter.TokenRelayFilterFunctions.tokenRelay;
import static org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions.lb;

@Configuration
public class GatewayConfig {

    @Bean
    public RouterFunction<ServerResponse> aiServiceRoute() {
        return route("ai-service")
                .route(path("/api/ai/**"), http())
                .filter(lb("ai-service"))
                .filter(tokenRelay())
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> userServiceRoute() {
        return route("user-service")
                .route(path("/api/users/**"), http())
                .filter(lb("user-service"))
                .filter(tokenRelay())
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> restaurantServiceRoute() {
        return route("restaurant-service")
                .route(path("/api/restaurants/**"), http())
                .filter(lb("restaurant-service"))
                .filter(tokenRelay())
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> locationServiceRoute() {
        return route("location-service")
                .route(path("/api/locations/**"), http())
                .filter(lb("location-service"))
                .filter(tokenRelay())
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> recommendationServiceRoute() {
        return route("recommendation-service")
                .route(path("/api/recommendations/**"), http())
                .filter(lb("recommendation-service"))
                .filter(tokenRelay())
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> reviewServiceRoute() {
        return route("review-service")
                .route(path("/api/reviews/**"), http())
                .filter(lb("review-service"))
                .filter(tokenRelay())
                .build();
    }
}
