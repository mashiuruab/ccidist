package com.cefalo.cci.service;

import static com.google.common.base.Preconditions.checkNotNull;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.cefalo.cci.model.token.AccessToken;
import com.cefalo.cci.model.token.ProductID;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.inject.Inject;

public class TokenGeneratorImpl implements TokenGenerator {
    private ApplicationConfiguration config;

    @Inject
    public TokenGeneratorImpl(ApplicationConfiguration config) {
        this.config = config;
    }

    @Override
    public AccessToken generateAccessToken(long timestamp, ProductID productId) {
        AccessToken dummyToken = new AccessToken(config.getAuthenticationSecret(), timestamp, productId);
        String signature = Hashing.md5().hashString(dummyToken.toString(), Charsets.UTF_8).toString();

        return new AccessToken(signature, timestamp, productId);
    }

    @Override
    public boolean isAuthorizedToken(AccessToken token) {
        checkNotNull(token);

        AccessToken generatedToken = generateAccessToken(token.getTimestamp(), token.getProductId());
        return token.matches(generatedToken);
    }

    @Override
    public boolean hasAccessToProduct(ProductID productId, AccessToken token) {
        return token.getProductId().matches(productId);
    }
}
