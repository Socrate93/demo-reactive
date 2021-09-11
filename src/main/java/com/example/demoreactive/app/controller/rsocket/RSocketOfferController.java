package com.example.demoreactive.app.controller.rsocket;

import com.example.demoreactive.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class RSocketOfferController {

  private final ReactiveMongoOperations operations;

  static {
    Hooks.onErrorDropped(e -> {});
  }

  private Flux<Product> products = generate();

  @MessageMapping(value = "products.all")
  public Flux<Product> getProducts() {
    long start = System.currentTimeMillis();
    return products.delayElements(Duration.ofMillis(5))
            .doOnTerminate(() -> System.out.println("Products in : " + (System.currentTimeMillis() - start)));
  }

  @MessageMapping(value = "products.related")
  public Flux<Product> getProductsRelatedTo(String id) {
    long start = System.currentTimeMillis();
    return products
            .filter(product -> Integer.parseInt(product.getId()) > Integer.parseInt(id))
            .take(5)
            .delayElements(Duration.ofMillis(20))
            .doOnTerminate(() -> System.out.println("Related of "+id+" in : " + (System.currentTimeMillis() - start)));
  }

  @MessageMapping(value = "products.related.batch")
  public Flux<ProductWithRelated> getProductsRelatedTo(Flux<String> ids) {
    long start = System.currentTimeMillis();
    return ids
            .concatMap(id -> products
                    .filter(product -> Integer.parseInt(product.getId()) > Integer.parseInt(id))
                    .take(5)
                    .collectList()
                    .map(list -> new ProductWithRelated(id, list)))
            .delayElements(Duration.ofMillis(20))
            .doOnTerminate(() -> System.out.println("Related in : " + (System.currentTimeMillis() - start)));
  }

  @MessageMapping(value = "products.stock")
  public Mono<Integer> getProductStock(String id) {
    long start = System.currentTimeMillis();
    return Mono.just(Integer.parseInt(id) - 5)
            .delayElement(Duration.ofMillis(50))
            .doOnTerminate(() -> System.out.println("Stock of "+id+" in : " + (System.currentTimeMillis() - start)));
  }

  @MessageMapping(value = "products.stock.batch")
  public Flux<ProductWithQuantity> getProductsStocks(Flux<String> ids) {
    long start = System.currentTimeMillis();
    return ids
            .map(id -> new ProductWithQuantity(id, Integer.parseInt(id) - 5))
            .delayElements(Duration.ofMillis(10))
            .doOnTerminate(() -> System.out.println("Stocks in : " + (System.currentTimeMillis() - start)));
  }

  private Flux<Product> generate() {
    return Flux.range(0, 20)
            .map(i -> new Product(String.valueOf(i), "Product "+i));
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Document
  private static class ProductWithRelated {
    @Id private String id;
    private List<Product> products;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Document
  private static class ProductWithQuantity {
    @Id private String id;
    private Integer stock;
  }
}
