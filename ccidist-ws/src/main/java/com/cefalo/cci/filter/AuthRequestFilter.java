package com.cefalo.cci.filter;

import static com.cefalo.cci.utils.StringUtils.isBlank;
import static com.cefalo.cci.utils.StringUtils.urlDecode;

import java.util.Objects;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Cookie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.cefalo.cci.model.token.AccessToken;
import com.cefalo.cci.service.TokenGenerator;
import com.google.inject.Inject;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

public class AuthRequestFilter implements ContainerRequestFilter {
    private final Logger logger = LoggerFactory.getLogger(AuthRequestFilter.class);

    static String AUTH_TOKEN_NAME = "accesstoken";

    @Inject
    private TokenGenerator tokenGenerator;

    @Inject
    private ApplicationConfiguration config;

    @Override
    public ContainerRequest filter(ContainerRequest request) {
        if (config.skipTokenBasedAuthentication()) {
            return request;
        }

        String accessTokenString = getAccessToken(request);
        if (!isBlank(accessTokenString)) {
            try {
                AccessToken accessToken = AccessToken.from(urlDecode(accessTokenString));
                if (tokenGenerator.isAuthorizedToken(accessToken)) {
                    // Set the access token on the request properties.
                    request.getProperties().put(AUTH_TOKEN_NAME, accessToken);
                }
            } catch (Exception ex) {
                logger.warn("Something went wrong in validating the access token: {}", accessTokenString);
            }
        }

        return request;
    }

    private String getAccessToken(ContainerRequest request) throws WebApplicationException {
        String tokenFromUrl = request.getQueryParameters().getFirst(AUTH_TOKEN_NAME);

        String tokenFromCookie = null;
        Cookie cookie = request.getCookies().get(AUTH_TOKEN_NAME);
        if (cookie != null) {
            tokenFromCookie = cookie.getValue();
        }

        // FIXME: Is this OK with Jess??
        if (!isBlank(tokenFromCookie) && !isBlank(tokenFromUrl) && !Objects.equals(tokenFromCookie, tokenFromUrl)) {
            logger.warn(
                    "Auth token values don't match between the URL & the Cookie. URL: {}, Cookie: {}. "
                            + "The one from the URL will be used.",
                    tokenFromUrl, tokenFromCookie);
        }

        // The token in URL has precedence.
        return isBlank(tokenFromUrl) ? tokenFromCookie : tokenFromUrl;
    }
}
