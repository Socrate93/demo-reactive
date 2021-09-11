package com.example.demoreactive.app.controller.rsocket;

import com.example.demoreactive.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequiredArgsConstructor
public class RSocketOfferController {

  private final ReactiveMongoOperations operations;

  @MessageMapping(value = "products.all")
  public Flux<Product> getProducts() {
    long start = System.currentTimeMillis();
    return operations.findAll(Product.class)
            .take(20)
            //.doOnTerminate(() -> System.out.println("Products all in : " + (System.currentTimeMillis() - start)))
            ;
  }

  @MessageMapping(value = "products.single")
  public Mono<Product> getProduct(String id) {
    long start = System.currentTimeMillis();
    return operations.findById(id, Product.class)
            //.doOnTerminate(() -> System.out.println("Product in : " + (System.currentTimeMillis() - start)))
            ;
  }

  @MessageMapping(value = "products.batch.stream")
  public Flux<Product> getProductsByIdsFlux(Flux<String> ids) {
    long start = System.currentTimeMillis();
    return ids.collectList()
            .flatMapMany(_ids -> operations.find(Query.query(Criteria.where("_id").in(_ids)), Product.class))
            //.doOnTerminate(() -> System.out.println("Products by ids in : " + (System.currentTimeMillis() - start)))
            ;
  }

  @MessageMapping(value = "products.batch")
  public Flux<Product> getProductsByIdsList(List<String> ids) {
    long start = System.currentTimeMillis();
    return operations.find(Query.query(Criteria.where("_id").in(ids)), Product.class)
            .doOnTerminate(() -> System.out.println("Products in : " + (System.currentTimeMillis() - start)));
  }

  @MessageMapping(value = "products.related")
  public Flux<String> getProductsRelatedTo(String id) {
    long start = System.currentTimeMillis();
    return operations.findById(id, ProductWithRelated.class)
            .flatMapMany(productWithRelated -> Flux.fromIterable(productWithRelated.getProducts()))
            .doOnTerminate(() -> System.out.println("Related of "+id+" in : " + (System.currentTimeMillis() - start)));
  }

  @MessageMapping(value = "products.related.batch")
  public Flux<ProductWithRelated> getProductsRelatedTo(Flux<String> ids) {
    long start = System.currentTimeMillis();
    return ids
            .concatMap(id -> operations.findById(id, ProductWithRelated.class))
            //.doOnTerminate(() -> System.out.println("Related in : " + (System.currentTimeMillis() - start)))
            ;
  }

  @MessageMapping(value = "products.stock")
  public Mono<Integer> getProductStock(String id) {
    long start = System.currentTimeMillis();
    return operations.findById(id, ProductWithQuantity.class)
            .map(ProductWithQuantity::getStock)
            //.doOnTerminate(() -> System.out.println("Stock of "+id+" in : " + (System.currentTimeMillis() - start)))
            ;
  }

  @MessageMapping(value = "products.stock.batch")
  public Flux<ProductWithQuantity> getProductsStocks(Flux<String> ids) {
    long start = System.currentTimeMillis();
    return ids.collectList()
            .flatMapMany(_ids -> operations.find(Query.query(Criteria.where("_id").in(_ids)), ProductWithQuantity.class))
            //.doOnTerminate(() -> System.out.println("Stocks in : " + (System.currentTimeMillis() - start)))
            ;
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
    private List<String> products;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Document
  private static class ProductWithQuantity {
    @Id private String id;
    private Integer stock;
  }

  static {
    Hooks.onErrorDropped(e -> {});
  }

  @PostConstruct
  public void load() {
    int total = 40;
    Flux.range(1, total)
            .map(i -> new Product(String.valueOf(i), "Product "+i))
            .flatMap(operations::save)
            .map(product -> new ProductWithRelated(product.getId(),
                    generateRelated(product.getId(), total)))
            .flatMap(operations::save)
            .map(product -> new ProductWithQuantity(product.getId(), Integer.parseInt(product.getId()) - 5))
            .flatMap(operations::save)
            .doOnTerminate(() -> System.out.println("Data Loaded"))
            .subscribe()
    ;
  }

  private List<String> generateRelated(String id, int total) {
    int n = Integer.parseInt(id);
    if (total <= n) {
      return List.of();
    }
    return IntStream.range(n + 1, Math.min(total, n + 7))
            .mapToObj(String::valueOf)
            .collect(Collectors.toList());
  }

}
