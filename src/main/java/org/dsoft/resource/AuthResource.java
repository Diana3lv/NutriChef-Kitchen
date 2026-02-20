package org.dsoft.resource;


import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.dsoft.dto.AuthResponse;
import org.dsoft.dto.LoginRequest;
import org.dsoft.dto.RegisterRequest;
import org.dsoft.service.AuthService;

import java.util.Map;


@Path("/api/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {
    
    @Inject
    AuthService authService;

    @POST
    @Path("/register")
    public Response register(RegisterRequest request) {
        try {
            AuthResponse authResponse = authService.register(request);
            return Response.status(Response.Status.CREATED)
                    .entity(authResponse)
                    .build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/login")
    public Response login(LoginRequest request) {
        try {
            AuthResponse authResponse = authService.login(request);
            return Response.ok(authResponse).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }
}