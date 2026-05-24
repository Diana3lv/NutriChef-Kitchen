package org.dsoft.control.filter;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dsoft.control.StartupTimestampService;

/**
 * Validates that JWT tokens were issued after the server started.
 * This ensures all tokens are invalidated when the backend restarts.
 * Runs after JWT validation but before method execution.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class TokenStartupValidationFilter implements ContainerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(TokenStartupValidationFilter.class);
    
    @Inject
    StartupTimestampService startupTimestampService;
    
    @Inject
    JsonWebToken jwt;
    
    @Override
    public void filter(ContainerRequestContext requestContext) {
        // Skip if no JWT is injected or if it has no claims (unauthenticated endpoints)
        if (jwt == null || jwt.getClaimNames() == null) {
            return;
        }
        
        // Also skip if the token doesn't have an "iat" claim, though this is unlikely for valid tokens
        if (!jwt.getClaimNames().contains("iat")) {
            return;
        }
        
        try {
            // Get the token's issued-at timestamp (in seconds)
            long tokenIssuedAt = jwt.getIssuedAtTime();
            long serverStartup = startupTimestampService.getStartupTimestamp();
            
            logger.debug("Token iat: {}, Server startup: {}", tokenIssuedAt, serverStartup);
            
            // If token was issued before server startup, reject it
            if (tokenIssuedAt < serverStartup) {
                logger.warn("Rejecting token issued before server startup (token iat: {}, startup: {})", 
                    tokenIssuedAt, serverStartup);
                requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\": \"Session expired due to server restart. Please log in again.\"}")
                        .build()
                );
            }
        } catch (Exception e) {
            logger.error("Error validating token startup time: {}", e.getMessage());
            // On error, reject the request to be safe
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Authentication validation failed\"}")
                    .build()
            );
        }
    }
}
