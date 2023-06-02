package com.arkson.apigatewaydemo.filters;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LocaleGatewayFilterFactory extends AbstractGatewayFilterFactory<LocaleGatewayFilterFactory.Config> {

    public LocaleGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (exchange.getRequest().getHeaders()
                    .getAcceptLanguage().isEmpty()) {
                var queryParamLocale = exchange
                        .getRequest()
                        .getQueryParams()
                        .getFirst("locale");

                var requestLocale = Optional.ofNullable(queryParamLocale)
                        .map(Locale::forLanguageTag)
                        .orElse(config.getDefaultLocale());

                exchange
                        .getRequest()
                        .mutate()
                        .headers(h -> h.setAcceptLanguageAsLocales(Collections.singletonList(requestLocale)));
            }

            var allOutgoingRequestLanguages =
                    exchange.getRequest()
                            .getHeaders()
                            .getAcceptLanguage()
                            .stream()
                            .map(Locale.LanguageRange::getRange)
                            .collect(Collectors.joining(","));

            log.info("Modify request output - Request contains Accept-Language header: {}", allOutgoingRequestLanguages);

            //remove all query params
            var modifiedExchange =
                    exchange
                            .mutate()
                            .request(originalRequest ->
                                    originalRequest
                                            .uri(UriComponentsBuilder
                                                    .fromUri(exchange.getRequest().getURI())
                                                    .replaceQueryParams(new LinkedMultiValueMap<>())
                                                    .build()
                                                    .toUri()))
                            .build();

            log.info("Removed all query param: {}", modifiedExchange.getRequest().getURI());

            return chain.filter(modifiedExchange);
        };
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Config {
        private Locale defaultLocale;
    }

}
