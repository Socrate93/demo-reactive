package com.example.demoreactive.app.controller;

import com.example.demoreactive.domain.Product;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Random;

@RestController
@RequestMapping("/products")
public class ProductController {

  private Flux<Product> products = generate();

  @GetMapping
  public Flux<Product> getProducts() {
    return products.delayElements(Duration.ofMillis(10));
  }

  @GetMapping(value = "/related-to/{id}")
  public Flux<Product> getProductsRelatedTo(@PathVariable String id) {
    return products.filter(product -> Integer.parseInt(product.getId()) > Integer.parseInt(id))
            .take(10)
            .delayElements(Duration.ofMillis(10));
  }

  @GetMapping(value = "/{id}/stocks")
  public Mono<Integer> getProductsStock(@PathVariable String id) {
    return Mono.just(new Random().nextInt(10) - 5)
            .delayElement(Duration.ofMillis(100));
  }

  private Flux<Product> generate() {
    return Flux.range(0, 40)
            .map(i -> new Product(String.valueOf(i), "Product "+i))
            ;
  }
}
