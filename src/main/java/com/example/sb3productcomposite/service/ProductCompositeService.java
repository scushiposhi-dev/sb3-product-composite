package com.example.sb3productcomposite.service;

import com.example.sb3productcomposite.util.ServiceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.Product;
import org.openapitools.model.ProductComposite;
import org.openapitools.model.Recommendation;
import org.openapitools.model.ServiceAddresses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ProductCompositeService {
    private final ProductCompositeIntegration productCompositeIntegration;
    private final ServiceUtil serviceUtil;

    public Mono<Void> createProductComposite(Mono<ProductComposite> productComposite) {
        Mono<ProductComposite> shareable = productComposite.share();

        return shareable.flatMapIterable(pc -> pc.getRecommendations())
                .flatMap(productCompositeIntegration::createRecommendation)
                .zipWith(shareable.map(p -> p.getProduct()).flatMap(productCompositeIntegration::createProduct)).then();
    }

    public Mono<Void> deleteProductCompositeByProductId(Integer productId) {
        return productCompositeIntegration.deleteProduct(productId)
                .zipWith(productCompositeIntegration.deleteRecommendations(productId))
                .then();
    }

    public Mono<ProductComposite> getByProductId(Integer productId) {
        return Mono.zip(values -> createComposite((Product) values[0], (List<Recommendation>) values[1]),
                        productCompositeIntegration.getProduct(productId),
                        productCompositeIntegration.getRecommendations(productId).collectList())
                .doOnError(ex -> log.warn("getComposite failed:{}", ex.getMessage()))
                .log(log.getName(), Level.FINE);
    }

    private ProductComposite createComposite(Product product, List<Recommendation> recommendations) {
        return ProductComposite.builder()
                .product(product)
                .serviceAddress(ServiceAddresses.builder()
                        .cmp(serviceUtil.getServiceAddress())
                        .pro(product.getServiceAddress())
                        .rec(!recommendations.isEmpty() ? recommendations.get(0).getServiceAddress() : "")
                        .rev("").build())
                .recommendations(recommendations).build();
    }
}