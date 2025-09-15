package com.example.bankingplatfrommonolit.Integration.config;

import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;

import java.nio.charset.StandardCharsets;

/**
 *   ---------- Тестовая конфигурация: лог HTTP -----------
 */
@Configuration
@Profile("test")
public class TestSupport {
    @Bean
    RestTemplateCustomizer restTemplateHttpLogger() {
        return rest -> {
            // Буферизуем тело, чтобы можно было читать в интерсепторе и ещё раз в RestTemplate
            if (!(rest.getRequestFactory() instanceof BufferingClientHttpRequestFactory)) {
                rest.setRequestFactory(
                        new BufferingClientHttpRequestFactory(new JdkClientHttpRequestFactory())
                );
            }
            // Добавляем интерсептор один раз
            var already = rest.getInterceptors().stream()
                    .anyMatch(i -> i.getClass().getName().contains("HttpLogInterceptor"));
            if (!already) {
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
                            ? "<empty>"
                            : new String(body, StandardCharsets.UTF_8)));

                    var response = execution.execute(request, body);
                    var bytes = org.springframework.util.StreamUtils.copyToByteArray(response.getBody());

                    System.out.println("<<< STATUS " + response.getStatusCode().value());
                    System.out.println("<<< BODY " + new String(bytes, StandardCharsets.UTF_8));

                    return response;
                });
            }
        };
    }
}
