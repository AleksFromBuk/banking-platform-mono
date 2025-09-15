package com.example.bankingplatfrommonolit.Integration.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@TestConfiguration
@Profile("test")
public class TestHttpClientConfig {

    @Bean
    RestTemplateCustomizer httpClientAndLogger() {
        return (RestTemplate rest) -> {
            //для PATCH; 4xx/5xx корректно читаются
            rest.setRequestFactory(
                    new BufferingClientHttpRequestFactory(new JdkClientHttpRequestFactory())
            );

            // простой логгер
            rest.getInterceptors().add((request, body, execution) -> {
                System.out.println(">>> OUT " + request.getMethod() + " " + request.getURI());
                request.getHeaders().forEach((k, v) -> {
                    if ("Authorization".equalsIgnoreCase(k)) {
                        System.out.println(">>> H Authorization: ***redacted***");
                    } else {
                        System.out.println(">>> H " + k + ": " + v);
                    }
                });
                System.out.println(">>> BODY " + (body == null || body.length == 0
                        ? "<empty>" : new String(body, java.nio.charset.StandardCharsets.UTF_8)));

                var response = execution.execute(request, body);

                String bodyText;
                try {
                    byte[] bytes = StreamUtils.copyToByteArray(response.getBody());
                    bodyText = new String(bytes, StandardCharsets.UTF_8);
                } catch (Exception ex) {
                    bodyText = "<unavailable: " + ex.getClass().getSimpleName() + ">";
                }

                try {
                    System.out.println("<<< STATUS " + response.getStatusCode().value());
                } catch (Exception ignore) {
                    System.out.println("<<< STATUS <unavailable>");
                }
                System.out.println("<<< BODY " + bodyText);

                return response;
            });
        };
    }
}
