package org.dsoft.boundary;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.dsoft.control.UserCollectionService;
import org.dsoft.control.InventoryService;
import org.dsoft.entity.dto.RecipeDTO;
import org.dsoft.entity.model.RecipeCollectionType;
import org.dsoft.service.ShoppingListService;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;

@Path("/api/users/collections")
@RequestScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserCollectionController {

    @Inject
    UserCollectionService collectionService;

    @Inject
    InventoryService inventoryService;

    @Inject
    ShoppingListService shoppingListService;

    @Inject
    JsonWebToken jwt;


    @GET
    @Path("/favorites")
    @RolesAllowed({"USER", "ADMIN"})
    public List<RecipeDTO> getFavorites() {
        return collectionService.getCollection(getCurrentUserId(), RecipeCollectionType.FAVORITE);
    }

    @GET
    @Path("/favorites/ids")
    @RolesAllowed({"USER", "ADMIN"})
    public List<Long> getFavoriteIds() {
        return collectionService.getCollectionIds(getCurrentUserId(), RecipeCollectionType.FAVORITE);
    }

    @POST
    @Path("/favorites/{recipeId}")
    @RolesAllowed({"USER", "ADMIN"})
    public Response addFavorite(@PathParam("recipeId") Long recipeId) {
        collectionService.add(getCurrentUserId(), recipeId, RecipeCollectionType.FAVORITE);
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @Path("/favorites/{recipeId}")
    @RolesAllowed({"USER", "ADMIN"})
    public Response removeFavorite(@PathParam("recipeId") Long recipeId) {
        collectionService.remove(getCurrentUserId(), recipeId, RecipeCollectionType.FAVORITE);
        return Response.noContent().build();
    }

    @GET
    @Path("/done")
    @RolesAllowed({"USER", "ADMIN"})
    public List<RecipeDTO> getDone() {
        return collectionService.getCollection(getCurrentUserId(), RecipeCollectionType.DONE);
    }

    @GET
    @Path("/done/ids")
    @RolesAllowed({"USER", "ADMIN"})
    public List<Long> getDoneIds() {
        return collectionService.getCollectionIds(getCurrentUserId(), RecipeCollectionType.DONE);
    }

    @POST
    @Path("/done/{recipeId}")
    @RolesAllowed({"USER", "ADMIN"})
    public Response markDone(@PathParam("recipeId") Long recipeId) {
        Long userId = getCurrentUserId();
        collectionService.add(userId, recipeId, RecipeCollectionType.DONE);
        try {
            shoppingListService.reduceBaseQuantityForCookedRecipe(userId, recipeId);
            inventoryService.subtractRecipeIngredients(userId, recipeId);
            shoppingListService.syncWithInventory(userId);
        } catch (Exception ignored) {
            // Inventory/sync failures do not block the collection update
        }
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @Path("/done/{recipeId}")
    @RolesAllowed({"USER", "ADMIN"})
    public Response unmarkDone(@PathParam("recipeId") Long recipeId) {
        collectionService.remove(getCurrentUserId(), recipeId, RecipeCollectionType.DONE);
        return Response.noContent().build();
    }

    private Long getCurrentUserId() {
        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new NotAuthorizedException("Missing JWT subject");
        }
        return Long.parseLong(subject);
    }
}