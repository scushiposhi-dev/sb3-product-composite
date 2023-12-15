package com.example.sb3productcomposite.operation;

import com.example.sb3productcomposite.service.ProductCompositeIntegration;
import com.example.sb3productcomposite.service.ProductCompositeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.api.ProductCompositeServiceApi;
import org.openapitools.model.ProductComposite;
import org.openapitools.model.Recommendation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ProductCompositeOperation implements ProductCompositeServiceApi {
    private final ProductCompositeService productCompositeService;

    @Override
    public Mono<Void> createProduct(Mono<ProductComposite> productComposite, ServerWebExchange exchange) {
        return productCompositeService.createProductComposite(productComposite);
    }

    @Override
    public Mono<Void> deleteByProductId(Integer productId, ServerWebExchange exchange) {
        return productCompositeService.deleteProductCompositeByProductId(productId);
    }

    @Override
    public Mono<ProductComposite> getByProductId(Integer productId, ServerWebExchange exchange) {
        return productCompositeService.getByProductId(productId);
    }
}
