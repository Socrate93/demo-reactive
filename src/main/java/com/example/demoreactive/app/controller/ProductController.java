package com.example.demoreactive.app.controller;

import com.example.demoreactive.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/products")
public class ProductController {

  private Flux<Product> products = generate();

  @GetMapping
  public Flux<Product> getProducts() {
    long start = System.currentTimeMillis();
    return products.delayElements(Duration.ofMillis(5))
            .doOnTerminate(() -> System.out.println("Products in : " + (System.currentTimeMillis() - start)));
  }

  @GetMapping(value = "/{id}/related")
  public Flux<Product> getProductsRelatedTo(@PathVariable String id) {
    long start = System.currentTimeMillis();
    return products
            .filter(product -> Integer.parseInt(product.getId()) > Integer.parseInt(id))
            .take(5)
            .delayElements(Duration.ofMillis(20))
            .doOnTerminate(() -> System.out.println("Related of "+id+" in : " + (System.currentTimeMillis() - start)));
  }

  @GetMapping(value = "/related")
  public Flux<ProductWithRelated> getProductsRelatedTo(@RequestParam Set<String> ids) {
    long start = System.currentTimeMillis();
    return Flux.fromIterable(ids)
            .flatMap(id -> products
              .filter(product -> Integer.parseInt(product.getId()) > Integer.parseInt(id))
              .take(5)
              .collectList()
            .map(list -> new ProductWithRelated(id, list)))
            .delayElements(Duration.ofMillis(20))
            .doOnTerminate(() -> System.out.println("Related of "+ids+" in : " + (System.currentTimeMillis() - start)));
  }

  @GetMapping(value = "/{id}/stocks")
  public Mono<Integer> getProductStock(@PathVariable String id) {
    long start = System.currentTimeMillis();
    return Mono.just(Integer.parseInt(id) - 5)
            .delayElement(Duration.ofMillis(50))
            .doOnTerminate(() -> System.out.println("Stocks of "+id+" in : " + (System.currentTimeMillis() - start)));
  }

  @GetMapping(value = "/stocks")
  public Flux<ProductWithQuantity> getProductsStocks(@RequestParam Set<String> ids) {
    long start = System.currentTimeMillis();
    return Flux.fromIterable(ids)
            .map(id -> new ProductWithQuantity(id, Integer.parseInt(id) - 5))
            .delayElements(Duration.ofMillis(10))
            .doOnTerminate(() -> System.out.println("Stocks of "+ids+" in : " + (System.currentTimeMillis() - start)));
  }

  private Flux<Product> generate() {
    return Flux.range(0, 20)
            .map(i -> new Product(String.valueOf(i), "Product "+i));
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  private static class ProductWithRelated {
    private String id;
    private List<Product> products;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  private static class ProductWithQuantity {
    private String id;
    private Integer stock;
  }
}
