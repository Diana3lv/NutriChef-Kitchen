package org.dsoft.config;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.util.Arrays;
import java.util.List;


@Provider
public class CorsFilter implements ContainerResponseFilter {

    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
        "http://localhost:4200",
        "http://127.0.0.1:4200"
    );

    private static final String ALLOW_CREDENTIALS = "true";
    private static final String ALLOW_HEADERS = "origin, content-type, accept, authorization";
    private static final String ALLOW_METHODS = "GET, POST, PUT, DELETE, OPTIONS, HEAD";
    private static final String MAX_AGE = "86400"; // 24 hours

    @Override
    public void filter(ContainerRequestContext requestContext,
                      ContainerResponseContext responseContext) {
        
        String origin = requestContext.getHeaderString("Origin");
        String allowedOrigin = getAllowedOrigin(origin);
        
        responseContext.getHeaders().add("Access-Control-Allow-Origin", allowedOrigin);
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", ALLOW_CREDENTIALS);
        responseContext.getHeaders().add("Access-Control-Allow-Headers", ALLOW_HEADERS);
        responseContext.getHeaders().add("Access-Control-Allow-Methods", ALLOW_METHODS);
        responseContext.getHeaders().add("Access-Control-Max-Age", MAX_AGE);
    }

    private String getAllowedOrigin(String requestOrigin) {
        if (requestOrigin != null && ALLOWED_ORIGINS.contains(requestOrigin)) {
            return requestOrigin;
        }
        return ALLOWED_ORIGINS.get(0);
    }
}
