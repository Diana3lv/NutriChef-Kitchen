package org.dsoft.boundary;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import org.dsoft.control.InventoryService;
import org.dsoft.control.RecipeStatusService;
import org.dsoft.entity.dto.SubmitFeedbackRequest;
import org.dsoft.entity.dto.UserRecipeStatusDTO;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/api/users/recipes")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RecipeStatusController {

    @Inject
    RecipeStatusService recipeStatusService;

    @Inject
    InventoryService inventoryService;

    @Inject
    JsonWebToken jwt;

    /** GET /api/users/recipes/{recipeId}/status */
    @GET
    @Path("/{recipeId}/status")
    @RolesAllowed({"USER", "ADMIN"})
    public UserRecipeStatusDTO getStatus(@PathParam("recipeId") Long recipeId) {
        return recipeStatusService.getStatusForRecipe(getCurrentUserId(), recipeId);
    }

    /** GET /api/users/recipes/in-progress */
    @GET
    @Path("/in-progress")
    @RolesAllowed({"USER", "ADMIN"})
    public List<UserRecipeStatusDTO> getInProgress() {
        return recipeStatusService.getInProgress(getCurrentUserId());
    }

    /** POST /api/users/recipes/{recipeId}/in-progress */
    @POST
    @Path("/{recipeId}/in-progress")
    @RolesAllowed({"USER", "ADMIN"})
    public Response markInProgress(@PathParam("recipeId") Long recipeId) {
        try {
            UserRecipeStatusDTO dto = recipeStatusService.markInProgress(getCurrentUserId(), recipeId);
            return Response.status(Response.Status.CREATED).entity(dto).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    /** DELETE /api/users/recipes/{recipeId}/in-progress */
    @DELETE
    @Path("/{recipeId}/in-progress")
    @RolesAllowed({"USER", "ADMIN"})
    public Response cancelInProgress(@PathParam("recipeId") Long recipeId) {
        recipeStatusService.cancelInProgress(getCurrentUserId(), recipeId);
        return Response.noContent().build();
    }

    /** GET /api/users/recipes/{recipeId}/can-complete — inventory pre-check (no side effects) */
    @GET
    @Path("/{recipeId}/can-complete")
    @RolesAllowed({"USER", "ADMIN"})
    public Response canComplete(@PathParam("recipeId") Long recipeId) {
        List<InventoryService.MissingIngredientInfo> missing =
            inventoryService.validateRecipeIngredientsAvailable(getCurrentUserId(), recipeId);
        if (!missing.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("missingIngredients", missing))
                .build();
        }
        return Response.ok(Map.of("canComplete", true)).build();
    }

    /** POST /api/users/recipes/{recipeId}/done */
    @POST
    @Path("/{recipeId}/done")
    @RolesAllowed({"USER", "ADMIN"})
    public Response markDone(
        @PathParam("recipeId") Long recipeId,
        SubmitFeedbackRequest feedback
    ) {
        // Run validation here to return structured JSON with the missing ingredient list
        List<InventoryService.MissingIngredientInfo> missing =
            inventoryService.validateRecipeIngredientsAvailable(getCurrentUserId(), recipeId);
        if (!missing.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("missingIngredients", missing))
                .build();
        }

        try {
            UserRecipeStatusDTO dto = recipeStatusService.markDone(getCurrentUserId(), recipeId, feedback);
            return Response.ok(dto).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    /** GET /api/users/recipes/done */
    @GET
    @Path("/done")
    @RolesAllowed({"USER", "ADMIN"})
    public List<UserRecipeStatusDTO> getDone() {
        return recipeStatusService.getDone(getCurrentUserId());
    }

    private Long getCurrentUserId() {
        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new NotAuthorizedException("Missing JWT subject");
        }
        return Long.parseLong(subject);
    }
}
