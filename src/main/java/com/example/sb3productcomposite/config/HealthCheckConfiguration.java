package com.example.sb3productcomposite.config;

import com.example.sb3productcomposite.service.ProductCompositeIntegration;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class HealthCheckConfiguration {
    @Bean
    ReactiveHealthContributor coreServices(ProductCompositeIntegration productCompositeIntegration){
        final Map<String, ReactiveHealthIndicator> registry = new LinkedHashMap<>();
        registry.put("product",productCompositeIntegration::getProductHealth);
        registry.put("recommendation",productCompositeIntegration::getRecommendationHealth);
        registry.put("review",productCompositeIntegration::getReviewHealth);

        return CompositeReactiveHealthContributor.fromMap(registry);
    }
}
