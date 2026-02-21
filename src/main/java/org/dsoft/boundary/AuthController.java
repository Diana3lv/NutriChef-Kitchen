package org.dsoft.boundary;


import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import org.dsoft.entity.dto.AuthResponse;
import org.dsoft.entity.dto.LoginRequest;
import org.dsoft.entity.dto.RegisterRequest;
import org.dsoft.control.AuthService;


@Path("/api/auth")
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
    public AuthResponse login(LoginRequest request) {
        return authService.login(request);
    }
}