package com.cefalo.cci.service;

import com.cefalo.cci.model.token.AccessToken;
import com.cefalo.cci.model.token.ProductID;

public interface TokenGenerator {
    AccessToken generateAccessToken(long timestamp, ProductID productId);

    boolean isAuthorizedToken(AccessToken token);

    boolean hasAccessToProduct(ProductID productId, AccessToken token);
}
