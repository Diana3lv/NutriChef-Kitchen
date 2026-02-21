package org.dsoft.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class AuthenticationExceptionMapper implements ExceptionMapper<AuthenticationException> {
    
    @Override
    public Response toResponse(AuthenticationException exception) {
        ErrorResponse error = new ErrorResponse(
            exception.getMessage(),
            Response.Status.UNAUTHORIZED.getStatusCode()
        );
        
        return Response
            .status(Response.Status.UNAUTHORIZED)
            .entity(error)
            .build();
    }
}
