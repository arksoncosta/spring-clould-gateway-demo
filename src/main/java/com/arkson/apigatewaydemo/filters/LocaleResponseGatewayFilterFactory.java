package com.arkson.apigatewaydemo.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LocaleResponseGatewayFilterFactory extends AbstractGatewayFilterFactory<LocaleResponseGatewayFilterFactory.Config> {

    public LocaleResponseGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    var response = exchange.getResponse();

                    var responseContentLanguage = response.getHeaders()
                            .getContentLanguage()
                            .getCountry();

                    response
                            .getHeaders()
                            .add("Country-Header", responseContentLanguage);

                    log.info("Added custom header to Response");
                }));
    }

    static class Config {
    }

}
