package com.arkson.apigatewaydemo.filters;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class LoggingGatewayFilterFactory extends AbstractGatewayFilterFactory<LoggingGatewayFilterFactory.Config> {

    public LoggingGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new OrderedGatewayFilter(
                this.executeLoggingLogic(config),
                1
        );
    }

    private GatewayFilter executeLoggingLogic(Config config) {
        return (exchange, chain) -> {
            if (config.isPreLogger()) {
                log.info("Pre GatewayFilter logging: {}", config.getBaseMessage());
            }

            return chain.filter(exchange)
                    .then(Mono.fromRunnable(() -> {
                        if (config.isPostLogger()) {
                            log.info("Post GatewayFilter logging: {}", config.getBaseMessage());
                        }
                    }));
        };
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("baseMessage", "preLogger", "postLogger");
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Config {
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }

}
