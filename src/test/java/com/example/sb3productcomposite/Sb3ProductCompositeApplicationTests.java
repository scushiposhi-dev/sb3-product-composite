package com.example.sb3productcomposite;

import com.example.sb3productcomposite.exceptions.InvalidInputException;
import com.example.sb3productcomposite.exceptions.NotFoundException;
import com.example.sb3productcomposite.service.ProductCompositeIntegration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.Product;
import org.openapitools.model.Recommendation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT,properties = {"eureka.client.enabled=false"})
class Sb3ProductCompositeApplicationTests {

    private static final int PRODUCT_ID_OK=1;
    private static final int PRODUCT_ID_NOT_FOUND=5;
    private static final int PRODUCT_ID_INVALID=3;
    public static final String NOT_FOUND_ID = "NOT FOUND ID:";
    public static final String INVALID_ID = "INVALID ID:";
    public static final String V_1_PRODUCT_COMPOSITE = "/v1/product-composite/";

    @Autowired
    WebTestClient client;

    @MockBean
    private ProductCompositeIntegration productCompositeIntegration;

    @BeforeEach
    void setUp() {
        when(productCompositeIntegration.getProduct(PRODUCT_ID_OK))
                .thenReturn(Mono.just(Product.builder().productId(PRODUCT_ID_OK).name("product id ok").build()));

        when(productCompositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
                .thenThrow(new NotFoundException(NOT_FOUND_ID +PRODUCT_ID_NOT_FOUND));

        when(productCompositeIntegration.getProduct(PRODUCT_ID_INVALID))
                .thenThrow(new InvalidInputException(INVALID_ID +PRODUCT_ID_INVALID));

        when(productCompositeIntegration.getRecommendations(PRODUCT_ID_OK))
                .thenReturn(Flux.fromIterable(Collections.singletonList(Recommendation.builder().build())));

    }

    @Test
    void contextLoads() {
       assertNotNull(client);
    }

    @Test
    void getProductByIdOK(){
        WebTestClient.BodyContentSpec bodyContentSpec = getAndVerity(PRODUCT_ID_OK, HttpStatus.OK);

        bodyContentSpec
                .jsonPath("$.product.productId").isEqualTo(PRODUCT_ID_OK)
                .jsonPath("$.recommendations.length()").isEqualTo(1);
    }
    @Test
    void getProductNotFound(){
        WebTestClient.BodyContentSpec bodyContentSpec = getAndVerity(PRODUCT_ID_NOT_FOUND, HttpStatus.NOT_FOUND);

        bodyContentSpec
                .jsonPath("$.path").isEqualTo(V_1_PRODUCT_COMPOSITE +PRODUCT_ID_NOT_FOUND)
                .jsonPath("$.message").isEqualTo(NOT_FOUND_ID+PRODUCT_ID_NOT_FOUND);
    }

    @Test
    void getProductInvalid(){
        WebTestClient.BodyContentSpec bodyContentSpec = getAndVerity(PRODUCT_ID_INVALID, HttpStatus.UNPROCESSABLE_ENTITY);
        bodyContentSpec
                .jsonPath("$.path").isEqualTo(V_1_PRODUCT_COMPOSITE+PRODUCT_ID_INVALID)
                .jsonPath("$.message").isEqualTo(INVALID_ID+PRODUCT_ID_INVALID);
    }


    private WebTestClient.BodyContentSpec getAndVerity(int productId, HttpStatus expctedStatus){
      return client.get().uri(V_1_PRODUCT_COMPOSITE+productId)
              .exchange()
              .expectStatus().isEqualTo(expctedStatus)
              .expectHeader().contentType(MediaType.APPLICATION_JSON)
              .expectBody();
    }

}