package org.dsoft.boundary;

import org.dsoft.control.InventoryService;
import org.dsoft.entity.dto.InventoryIngredientDTO;
import org.dsoft.service.ShoppingListService;
import org.eclipse.microprofile.jwt.JsonWebToken;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.List;

@Path("/api/users/inventory")
@RequestScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class InventoryController {

    @Inject
    InventoryService inventoryService;

    @Inject
    ShoppingListService shoppingListService;

    @Inject
    JsonWebToken jwt;

    @GET
    @RolesAllowed({"USER", "ADMIN"})
    public List<InventoryIngredientDTO> getInventory() {
        Long userId = getCurrentUserId();
        return inventoryService.getUserInventory(userId);
    }

    @POST
    @RolesAllowed({"USER", "ADMIN"})
    public Response addItem(InventoryIngredientDTO itemDTO) {
        if (itemDTO.getIngredient() == null || itemDTO.getIngredient().getName() == null || 
            itemDTO.getIngredient().getName().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Ingredient name is required")
                    .build();
        }

        Long userId = getCurrentUserId();
        InventoryIngredientDTO created = inventoryService.addItem(userId, itemDTO);
        shoppingListService.syncWithInventory(userId);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{itemId}")
    @RolesAllowed({"USER", "ADMIN"})
    public InventoryIngredientDTO updateItem(@PathParam("itemId") Long itemId, InventoryIngredientDTO itemDTO) {
        Long userId = getCurrentUserId();
        InventoryIngredientDTO result = inventoryService.updateItem(itemId, userId, itemDTO);
        shoppingListService.syncWithInventory(userId);
        return result;
    }

    @DELETE
    @Path("/{itemId}")
    @RolesAllowed({"USER", "ADMIN"})
    public Response deleteItem(@PathParam("itemId") Long itemId) {
        Long userId = getCurrentUserId();
        inventoryService.removeItem(itemId, userId);
        shoppingListService.syncWithInventory(userId);
        return Response.noContent().build();
    }

    @POST
    @Path("/expired/clear")
    @RolesAllowed({"USER", "ADMIN"})
    public Response clearExpired() {
        Long userId = getCurrentUserId();
        int count = inventoryService.clearExpiredItems(userId);
        shoppingListService.syncWithInventory(userId);
        return Response.ok().entity("Cleared " + count + " expired items.").build();
    }

    @POST
    @Path("/ingredient/{ingredientId}")
    @RolesAllowed({"USER", "ADMIN"})
    public Response addIngredientById(
            @PathParam("ingredientId") Long ingredientId,
            @QueryParam("quantity") String quantity,
            @QueryParam("notes") String notes,
            @QueryParam("expiryDate") String expiryDate) {
        if (quantity == null || quantity.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("quantity is required")
                    .build();
        }
        try {
            Long userId = getCurrentUserId();
            LocalDate expiry = (expiryDate != null && !expiryDate.isBlank())
                    ? LocalDate.parse(expiryDate) : null;
            InventoryIngredientDTO result =
                    inventoryService.addItemById(userId, ingredientId, quantity, notes, expiry);
            shoppingListService.syncWithInventory(userId);
            return Response.status(Response.Status.CREATED).entity(result).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    private Long getCurrentUserId() {
        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new NotAuthorizedException("Missing JWT subject");
        }

        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException e) {
            throw new NotAuthorizedException("Invalid JWT subject format");
        }
    }
}