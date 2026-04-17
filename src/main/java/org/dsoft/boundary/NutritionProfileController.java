package org.dsoft.boundary;

import org.dsoft.control.NutritionProfileService;
import org.dsoft.entity.dto.NutritionProfileDTO;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

@Path("/api/users/nutrition-profile-settings")
@RequestScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class NutritionProfileController {

    @Inject
    NutritionProfileService nutritionProfileService;

    @GET
    @RolesAllowed({"USER", "ADMIN"})
    public NutritionProfileDTO getProfileSettings(@Context SecurityContext securityContext) {
        Long userId = Long.parseLong(securityContext.getUserPrincipal().getName());
        return nutritionProfileService.getNutritionProfileByUserId(userId);
    }

    @PUT
    @RolesAllowed({"USER", "ADMIN"})
    public NutritionProfileDTO updateProfileSettings(@Context SecurityContext securityContext, NutritionProfileDTO request) {
        Long userId = Long.parseLong(securityContext.getUserPrincipal().getName());
        return nutritionProfileService.updateNutritionProfile(userId, request);
    }
}
