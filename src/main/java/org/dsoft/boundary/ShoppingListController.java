package org.dsoft.boundary;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;
import org.dsoft.entity.dto.ShoppingListItemDTO;
import org.dsoft.service.ShoppingListService;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/api/users/shopping-list")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ShoppingListController {

    @Inject
    ShoppingListService shoppingListService;

    @Inject
    JsonWebToken jwt;

    @GET
    @RolesAllowed({"USER", "ADMIN"})
    public Response getShoppingList() {
        try {
            Long userId = getCurrentUserId();
            List<ShoppingListItemDTO> items = shoppingListService.getShoppingList(userId);
            return Response.ok(items).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error fetching shopping list: " + e.getMessage())
                .build();
        }
    }

    @POST
    @Path("/recipe/{recipeId}")
    @RolesAllowed({"USER", "ADMIN"})
    public Response addRecipeIngredientsToList(
        @PathParam("recipeId") Long recipeId
    ) {
        try {
            Long userId = getCurrentUserId();
            shoppingListService.addRecipeIngredientsToList(userId, recipeId);
            return Response.status(Response.Status.CREATED)
                .entity("Recipe ingredients added to shopping list")
                .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(e.getMessage())
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error adding recipe to shopping list: " + e.getMessage())
                .build();
        }
    }

    @DELETE
    @Path("/recipe/{recipeId}")
    @RolesAllowed({"USER", "ADMIN"})
    public Response removeRecipeIngredientsFromList(
        @PathParam("recipeId") Long recipeId
    ) {
        try {
            Long userId = getCurrentUserId();
            shoppingListService.removeRecipeIngredientsFromList(userId, recipeId);
            return Response.ok("Recipe ingredients removed from shopping list").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error removing recipe from shopping list: " + e.getMessage())
                .build();
        }
    }

    @POST
    @RolesAllowed({"USER", "ADMIN"})
    public Response addItem(
        @QueryParam("ingredientId") Long ingredientId,
        @QueryParam("quantity") BigDecimal quantity,
        @QueryParam("notes") String notes
    ) {
        try {
            if (ingredientId == null || quantity == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("ingredientId and quantity are required")
                    .build();
            }

            Long userId = getCurrentUserId();
            ShoppingListItemDTO item = shoppingListService.addItem(
                userId,
                ingredientId,
                quantity,
                notes != null ? notes : ""
            );
            return Response.status(Response.Status.CREATED).entity(item).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(e.getMessage())
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error adding item: " + e.getMessage())
                .build();
        }
    }

    @DELETE
    @Path("/{ingredientId}")
    @RolesAllowed({"USER", "ADMIN"})
    public Response removeItem(
        @PathParam("ingredientId") Long ingredientId
    ) {
        try {
            Long userId = getCurrentUserId();
            shoppingListService.removeItem(userId, ingredientId);
            return Response.ok("Item removed from shopping list").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error removing item: " + e.getMessage())
                .build();
        }
    }

    @PUT
    @Path("/{itemId}/purchased")
    @RolesAllowed({"USER", "ADMIN"})
    public Response markBought(@PathParam("itemId") Long itemId) {
        try {
            Long userId = getCurrentUserId();
            shoppingListService.markBought(userId, itemId);
            return Response.ok("Item added to inventory").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error updating item: " + e.getMessage())
                .build();
        }
    }

    @DELETE
    @Path("/purchased")
    @RolesAllowed({"USER", "ADMIN"})
    public Response clearPurchased() {
        try {
            Long userId = getCurrentUserId();
            shoppingListService.clearPurchased(userId);
            return Response.ok("Purchased items cleared").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error clearing items: " + e.getMessage())
                .build();
        }
    }

    private Long getCurrentUserId() {
        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new NotAuthorizedException("Missing JWT subject");
        }
        return Long.parseLong(subject);
    }

    @PUT
    @Path("/{itemId}/quantity")
    @RolesAllowed({"USER", "ADMIN"})
    public Response updateItemQuantity(
        @PathParam("itemId") Long itemId,
        BigDecimal newQuantity
    ) {
        try {
            if (newQuantity == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("quantity is required")
                    .build();
            }
            Long userId = getCurrentUserId();
            ShoppingListItemDTO item = shoppingListService.updateItemQuantity(userId, itemId, newQuantity);
            return Response.ok(item).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(e.getMessage())
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error updating quantity: " + e.getMessage())
                .build();
        }
    }

    @DELETE
    @Path("/batch")
    @RolesAllowed({"USER", "ADMIN"})
    public Response batchRemoveItems(java.util.List<Long> ingredientIds) {
        try {
            if (ingredientIds == null || ingredientIds.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("ingredientIds list is required")
                    .build();
            }
            Long userId = getCurrentUserId();
            String error = shoppingListService.validateAndRemoveItems(userId, ingredientIds);
            if (error != null) {
                return Response.status(Response.Status.CONFLICT)
                    .entity(error)
                    .build();
            }
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error removing items: " + e.getMessage())
                .build();
        }
    }

    @PUT
    @Path("/{itemId}/checked")
    @RolesAllowed({"USER", "ADMIN"})
    public Response toggleChecked(@PathParam("itemId") Long itemId) {
        try {
            Long userId = getCurrentUserId();
            ShoppingListItemDTO item = shoppingListService.toggleChecked(userId, itemId);
            return Response.ok(item).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(e.getMessage())
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error toggling checked state: " + e.getMessage())
                .build();
        }
    }

    @PUT
    @Path("/{itemId}/own-quantity")
    @RolesAllowed({"USER", "ADMIN"})
    public Response reduceOwnQuantity(
        @PathParam("itemId") Long itemId,
        @QueryParam("amount") BigDecimal amount
    ) {
        try {
            if (amount == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("amount query parameter is required")
                    .build();
            }
            Long userId = getCurrentUserId();
            ShoppingListItemDTO result = shoppingListService.reduceOwnQuantity(userId, itemId, amount);
            if (result == null) {
                // Item was deleted (purely manual, own qty reached 0)
                return Response.noContent().build();
            }
            return Response.ok(result).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(e.getMessage())
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error reducing own quantity: " + e.getMessage())
                .build();
        }
    }
}
