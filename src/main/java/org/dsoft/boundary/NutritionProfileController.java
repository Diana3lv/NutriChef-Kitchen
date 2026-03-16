package org.dsoft.boundary;

import org.dsoft.control.NutritionProfileService;
import org.dsoft.entity.dto.NutritionProfileDTO;
import org.eclipse.microprofile.jwt.JsonWebToken;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/users/nutrition-profile-settings")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class NutritionProfileController {

    @Inject
    NutritionProfileService nutritionProfileService;

    @Inject
    JsonWebToken jwt;

    @GET
    @RolesAllowed({"USER", "ADMIN"})
    public NutritionProfileDTO getProfileSettings() {
        Long userId = getCurrentUserId();
        return nutritionProfileService.getNutritionProfileByUserId(userId);
    }

    @PUT
    @RolesAllowed({"USER", "ADMIN"})
    public NutritionProfileDTO updateProfileSettings(NutritionProfileDTO request) {
        Long userId = getCurrentUserId();
        nutritionProfileService.updateNutritionProfile(userId, request);
        return nutritionProfileService.getNutritionProfileByUserId(userId);
    }

    private Long getCurrentUserId() {
        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new NotAuthorizedException("Missing JWT subject");
        }

        try {
            return Long.valueOf(subject);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Invalid JWT subject format");
        }
    }
}
