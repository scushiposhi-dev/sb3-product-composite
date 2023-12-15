package com.example.sb3productcomposite;

import com.example.sb3productcomposite.message.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.openapitools.model.Product;
import org.openapitools.model.ProductComposite;
import org.openapitools.model.Recommendation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.ACCEPTED;

@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true","eureka.client.enabled=false"})
@Import({TestChannelBinderConfiguration.class})
@Slf4j
class MessagingTests {
    @Autowired
    private WebTestClient client;

    @Autowired
    private OutputDestination target;

    @Test
    void createCompositeProduct() throws JsonProcessingException {

        int productId=1;
        ProductComposite composite = getProductComposite(productId);

        postAndVerifyProduct(composite, ACCEPTED);
        final List<String> productMessages = getMessages("products");
        final List<String> recommendationMessages = getMessages("recommendations");
//        final List<String> reviewMessages = getMessages("reviews");

        // Assert one expected new product event queued up
        assertEquals(1, productMessages.size());
        assertEquals(2, recommendationMessages.size());

        Event<Integer, Product> productEvent = Event.<Integer, Product>builder()
                .eventType(Event.Type.CREATE)
                .eventCreatedAt(null)
                .data(composite.getProduct())
                .key(composite.getProduct().getProductId()).build();

        Event<Integer, Recommendation> recommendationEvent = Event.<Integer, Recommendation>builder()
                .eventType(Event.Type.CREATE)
                .eventCreatedAt(null)
                .data(composite.getRecommendations().get(0))
                .key(composite.getRecommendations().get(0).getProductId()).build();

        assertEquals(productMessages.get(0), new ObjectMapper().writeValueAsString(productEvent));
    }

    private ProductComposite getProductComposite(int productId) {
        Product product = Product.builder().productId(productId).name("my name").build();
        Recommendation recommendation1 = Recommendation.builder().productId(productId).recommendationId(1).author("me").build();
        Recommendation recommendation2 = Recommendation.builder().productId(productId).recommendationId(2).author("me again").build();
        return ProductComposite.builder()
                .product(product)
                .recommendations(Arrays.asList(recommendation1,recommendation2))
                .build();
    }

    private List<String> getMessages(String bindingName) {
        List<String> messages = new ArrayList<>();
        boolean anyMoreMessages = true;

        while (anyMoreMessages) {
            Message<byte[]> message = getMessage(bindingName);

            if (message == null) {
                anyMoreMessages = false;

            } else {
                messages.add(new String(message.getPayload()));
            }
        }
        return messages;
    }

    private Message<byte[]> getMessage(String bindingName) {
        try {
            return target.receive(0, bindingName);
        } catch (NullPointerException npe) {
            // If the messageQueues member variable in the target object contains no queues when the receive method is called, it will cause a NPE to be thrown.
            // So we catch the NPE here and return null to indicate that no messages were found.
            log.error("getMessage() received a NPE with binding = {}", bindingName);
            return null;
        }
    }

    private WebTestClient.BodyContentSpec postAndVerifyProduct(ProductComposite productComposite, HttpStatus expectedStatus) {
        return client.post().uri("/v1/product-composite")
                .body(Mono.just(productComposite), ProductComposite.class)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody();
    }

    private void deleteAndVerifyProduct(int productId, HttpStatus expectedStatus) {
        client.delete().uri("/v1/product-composite/" + productId)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }
}
