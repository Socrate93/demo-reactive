package com.example.demoreactive.app.controller;

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
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.*;
import static org.springframework.data.mongodb.core.query.Query.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

  private final ReactiveMongoOperations operations;

  @GetMapping
  public Flux<Product> getProducts() {
    long start = System.currentTimeMillis();
    return operations.findAll(Product.class)
            .take(40)
            //.doOnTerminate(() -> System.out.println("Products all in : " + (System.currentTimeMillis() - start)))
            ;
  }

  @GetMapping(value = "/{id}")
  public Mono<Product> getProduct(@PathVariable String id) {
    long start = System.currentTimeMillis();
    return operations.findById(id, Product.class)
            //.doOnTerminate(() -> System.out.println("Product in : " + (System.currentTimeMillis() - start)))
            ;
  }

  @PostMapping(value = "/searches")
  public Flux<Product> getProductsByIdsFlux(@RequestBody List<String> ids) {
    long start = System.currentTimeMillis();
    return operations.find(query(where("_id").in(ids)), Product.class)
            //.doOnTerminate(() -> System.out.println("Products by ids in : " + (System.currentTimeMillis() - start)))
            ;
  }

  @PostMapping(value = "/related/searches")
  public Flux<ProductWithRelated> getProductsRelatedTo(@RequestBody List<String> ids) {
    long start = System.currentTimeMillis();
    return operations.find(query(where("_id").in(ids)), ProductWithRelated.class)
            //.doOnTerminate(() -> System.out.println("Related in : " + (System.currentTimeMillis() - start)))
            ;
  }

  @PostMapping(value = "/stocks/searches")
  public Flux<ProductWithQuantity> getProductsStocks(@RequestBody List<String> ids) {
    long start = System.currentTimeMillis();
    return operations.find(query(where("_id").in(ids)), ProductWithQuantity.class)
            //.doOnTerminate(() -> System.out.println("Stocks in : " + (System.currentTimeMillis() - start)))
            ;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Document
  private static class ProductWithRelated {
    @Id
    private String id;
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

}
