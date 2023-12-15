package com.example.sb3productcomposite.service;

import com.example.sb3productcomposite.exceptions.InvalidInputException;
import com.example.sb3productcomposite.exceptions.NotFoundException;
import com.example.sb3productcomposite.message.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.HttpErrorInfo;
import org.openapitools.model.Product;
import org.openapitools.model.Recommendation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Objects;
import java.util.logging.Level;

@Component
@Slf4j
public class ProductCompositeIntegration {
    public static final String ACTUATOR_HEALTH = "/actuator/health";
    public static final String PARTITION_KEY = "partitionKey";
    private final ObjectMapper objectMapper;
    private final StreamBridge streamBridge;
    private final Scheduler publishEventScheduler;
    private final WebClient webClient;

    private static final String PRODUCT_URL = "http://product";
    private static final String RECOMMENDATION_URL = "http://recommendation";

    @Autowired
    public ProductCompositeIntegration(ObjectMapper objectMapper, StreamBridge streamBridge, WebClient.Builder webClientBuilder,
                                       @Qualifier("publishEventScheduler") Scheduler publishEventScheduler) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
        this.streamBridge = streamBridge;
        this.publishEventScheduler = publishEventScheduler;
    }


    public Mono<Product> createProduct(Product product) {

        return Mono.fromCallable(() -> {
            sendMessage("products-out-0",
                    Event.builder()
                            .data(product)
                            .key(product.getProductId())
                            .eventType(Event.Type.CREATE).build());
            return product;
        }).subscribeOn(publishEventScheduler);
    }

    public Mono<Void> deleteProduct(int productId) {
        return Mono.fromRunnable(() ->
                sendMessage("products-out-0",
                        Event.builder()
                                .key(productId)
                                .data(null)
                                .eventType(Event.Type.DELETE).build())
        ).subscribeOn(publishEventScheduler).then();
    }

    public Mono<Product> getProduct(int productId) {
        String url = PRODUCT_URL + "/v1/product/" + productId;

        return webClient.get()
                .uri(url)
                .retrieve().bodyToMono(Product.class)
                .log("getProduct", Level.FINE)
                .onErrorMap(WebClientResponseException.class, this::handleException);
    }


    public Mono<Recommendation> createRecommendation(Recommendation recommendation) {
        return Mono.fromCallable(() -> {
            sendMessage("recommendations-out-0",
                    Event.builder()
                            .data(recommendation)
                            .key(recommendation.getProductId())
                            .eventType(Event.Type.CREATE).build());
            return recommendation;
        }).subscribeOn(publishEventScheduler);
    }

    public Flux<Recommendation> getRecommendations(int productId) {
        String url = RECOMMENDATION_URL + "/v1/recommendation?productId=" + productId;

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Recommendation.class)
                .log("getRecommendations", Level.FINE)
                .onErrorResume(error -> Flux.empty());
    }

    public Mono<Void> deleteRecommendations(int productId) {
        return Mono.fromRunnable(() ->
                sendMessage("recommendations-out-0",
                        Event.builder()
                                .key(productId)
                                .data(null)
                                .eventType(Event.Type.DELETE).build())
        ).subscribeOn(publishEventScheduler).then();
    }

    private <K, D> void sendMessage(String bindingName, Event<K, D> event) {
        log.debug("sending {} message to {}", event.getEventType(), bindingName);

        Message<Event<K, D>> message = MessageBuilder.withPayload(event)
                .setHeader(PARTITION_KEY, event.getKey())
                .build();

        streamBridge.send(bindingName, message);
    }

    private Exception handleException(WebClientResponseException wcre) {
        switch (Objects.requireNonNull(HttpStatus.resolve(wcre.getStatusCode().value()))) {
            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(wcre));
            case UNPROCESSABLE_ENTITY:
                return new InvalidInputException(getErrorMessage(wcre));
            default:
                log.warn("Unexpected HTTP error occurred:{}", wcre.getStatusCode());
                log.warn("Error body:{}", wcre.getResponseBodyAsString());
                return wcre;
        }
    }

    private String getErrorMessage(WebClientResponseException webClientResponseException) {
        try {
            return objectMapper
                    .readValue(webClientResponseException.getResponseBodyAsString(), HttpErrorInfo.class)
                    .getMessage();
        } catch (JsonProcessingException e) {
            return e.getMessage();
        }
    }

    public Mono<Health> getProductHealth() {

        return getHealth(PRODUCT_URL);
    }

    public Mono<Health> getRecommendationHealth() {
        return getHealth(RECOMMENDATION_URL);
    }

    public Mono<Health> getReviewHealth() {
        return Mono.empty();
    }

    public Mono<Health> getHealth(String url) {
        url += ACTUATOR_HEALTH;

        log.debug("Will call the Health API on URL:{}", url);

        return webClient.get().uri(url).retrieve().bodyToMono(String.class)
                .map(s -> new Health.Builder().up().build())
                .onErrorResume(ex -> Mono.just(new Health.Builder().down().build()))
                .log(log.getName(), Level.FINE);
    }
}
