package com.example.demoreactive.app.controller.rsocket;

import io.rsocket.core.Resume;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.util.retry.Retry;

import java.time.Duration;

import static io.rsocket.frame.decoder.PayloadDecoder.ZERO_COPY;

@Configuration
@Slf4j
public class RSocketConfiguration {

  @Bean
  RSocketServerCustomizer rSocketResume() {
    var resume =
            new Resume()
                    .sessionDuration(Duration.ofMinutes(15))
                    .retry(
                            Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(1))
                                    .doBeforeRetry(s -> log.debug("Disconnected. Trying to resume...")));
    return rSocketServer -> rSocketServer.resume(resume).payloadDecoder(ZERO_COPY);
  }


}
