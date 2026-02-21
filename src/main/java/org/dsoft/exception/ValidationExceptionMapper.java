package org.dsoft.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {
    
    @Override
    public Response toResponse(ValidationException exception) {
        ErrorResponse error = new ErrorResponse(
            exception.getMessage(),
            Response.Status.BAD_REQUEST.getStatusCode()
        );
        
        return Response
            .status(Response.Status.BAD_REQUEST)
            .entity(error)
            .build();
    }
}
