package org.dsoft.boundary;


import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import io.quarkus.security.Authenticated;

import org.dsoft.entity.dto.AuthResponse;
import org.dsoft.entity.dto.LoginRequest;
import org.dsoft.entity.dto.RegisterRequest;
import org.dsoft.control.AuthService;


@Path("/api/auth")
@RequestScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthController {
    
    @Inject
    AuthService authService;

    @POST
    @Path("/register")
    public AuthResponse register(RegisterRequest request) {
        return authService.register(request);
    }

    @POST
    @Path("/login")
    public Response login(LoginRequest request) {
        AuthResponse response = authService.login(request);
        return Response.ok(response).build();
    }

    @GET
    @Path("/validate")
    @Authenticated
    public Response validateSession() {
        return Response.ok().build();
    }
}