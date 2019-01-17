package com.cefalo.cci.model.token;

import static com.cefalo.cci.utils.StringUtils.isBlank;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.ArrayList;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class AccessToken implements Serializable {
    private static final long serialVersionUID = -6266534639566188716L;

    private static final Splitter TOKEN_SPLITTER = Splitter.on('|').limit(3);

    private final String signature;
    private final long timestamp;
    private final ProductID productId;

    public AccessToken(String signature, long timestamp, ProductID productId) {
        checkArgument(!isBlank(signature));
        checkNotNull(productId);

        this.signature = signature;
        this.timestamp = timestamp;
        this.productId = productId;
    }

    public String getSignature() {
        return signature;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ProductID getProductId() {
        return productId;
    }

    public static AccessToken from(String token) {
        checkArgument(!isBlank(token));

        ArrayList<String> tokenParts = Lists.newArrayList(TOKEN_SPLITTER.split(token));
        if (tokenParts.size() != 3) {
            throw new IllegalArgumentException("Not a valid token string. Token: ".concat(token));
        }

        return new AccessToken(tokenParts.get(0), Long.valueOf(tokenParts.get(1)), ProductID.from(tokenParts.get(2)));
    }

    public boolean matches(AccessToken other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }

        if (!productId.matches(other.productId)) {
            return false;
        }

        if (!signature.equals(other.signature)) {
            return false;
        }

        if (timestamp != other.timestamp) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return String.format("%s|%s|%s", signature, timestamp, productId);
    }
}
