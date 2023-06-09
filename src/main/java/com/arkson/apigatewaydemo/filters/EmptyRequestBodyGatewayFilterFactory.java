package com.arkson.apigatewaydemo.filters;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

@Component
public class EmptyRequestBodyGatewayFilterFactory extends AbstractGatewayFilterFactory<EmptyRequestBodyGatewayFilterFactory.Config> {

    public EmptyRequestBodyGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            var modifyRequestBodyConfig =
                    new ModifyRequestBodyGatewayFilterFactory.Config()
                            .setContentType(MediaType.APPLICATION_JSON_VALUE)
                            .setRewriteFunction(Map.class, Map.class, (serverWebExchange, originalRequestBody) ->
                            {
                                if (originalRequestBody == null || originalRequestBody.isEmpty()) {
                                    return Mono.just(Collections.emptyMap());
                                }

                                return Mono.just(originalRequestBody);
                            });

            return new ModifyRequestBodyGatewayFilterFactory()
                    .apply(modifyRequestBodyConfig)
                    .filter(exchange, chain);
        };
    }

    static class Config {
    }

}
