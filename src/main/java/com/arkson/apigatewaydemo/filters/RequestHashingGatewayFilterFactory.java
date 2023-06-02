package com.arkson.apigatewaydemo.filters;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
@Slf4j
public class RequestHashingGatewayFilterFactory extends AbstractGatewayFilterFactory<RequestHashingGatewayFilterFactory.Config> {

    public RequestHashingGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        var digest = config.getMessageDigest();
        return new OrderedGatewayFilter(
                applyHashing(digest),
                1
        );
    }

    private GatewayFilter applyHashing(MessageDigest digest) {
        return (exchange, chain) -> {
            log.info("Applying customer filter with digest {}", digest.getAlgorithm());
            return chain.filter(exchange);
        };
    }

    private String computeHash(MessageDigest messageDigest, String requestPayload) {
        return Hex.toHexString(messageDigest.digest(requestPayload.getBytes()));
    }

    static class Config {
        private MessageDigest messageDigest;

        public MessageDigest getMessageDigest() {
            return messageDigest;
        }

        public void setAlgorithm(String algorithm) throws NoSuchAlgorithmException {
            messageDigest = MessageDigest.getInstance(algorithm);
        }
    }

}
