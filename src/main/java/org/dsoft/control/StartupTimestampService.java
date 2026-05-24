package org.dsoft.control;

import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores the server startup timestamp to invalidate all JWT tokens issued before restart.
 * This ensures all users are automatically logged out when the backend restarts.
 */
@ApplicationScoped
public class StartupTimestampService {
    
    private static final Logger logger = LoggerFactory.getLogger(StartupTimestampService.class);
    
    /**
     * Timestamp (in seconds since epoch) when the server started.
     * All tokens with iat (issued at) before this time are considered invalid.
     */
    private long startupTimestamp;
    
    public void setStartupTimestamp(long timestampSeconds) {
        this.startupTimestamp = timestampSeconds;
        logger.info("Server startup timestamp set to: {} ({})", timestampSeconds, new java.util.Date(timestampSeconds * 1000));
    }
    
    public long getStartupTimestamp() {
        return startupTimestamp;
    }
    
    /**
     * Check if a token (identified by its issued-at timestamp) is still valid.
     * A token is valid only if it was issued after the server started.
     * 
     * @param tokenIssuedAtSeconds Token's iat claim (issued-at) in seconds since epoch
     * @return true if token was issued after server startup, false otherwise
     */
    public boolean isTokenValid(long tokenIssuedAtSeconds) {
        return tokenIssuedAtSeconds >= startupTimestamp;
    }
}
