package org.dsoft.boundary;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.util.List;
import org.dsoft.control.RecipeService;
import org.dsoft.entity.model.Recipe;

@Path("/recipes")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RecipeController {

    @Inject
    RecipeService recipeService;

    @GET
    public List<Recipe> getAll() {
        return recipeService.getAll();
    }

    @GET
    @Path("/personalized")
    @RolesAllowed({"USER", "ADMIN"})
    public List<Recipe> getPersonalizedRecipes(@Context SecurityContext securityContext) {
        Long userId = Long.parseLong(securityContext.getUserPrincipal().getName());
        return recipeService.getRecipesForUserNutritionProfile(userId);
    }

    @GET
    @Path("/{id}")
    public Recipe getById(@PathParam("id") Long id) {
        return recipeService.getById(id)
                .orElseThrow(() -> new NotFoundException("Recipe not found"));
    }

    @POST
    public Response create(Recipe recipe) {
        recipeService.create(recipe);
        return Response.status(Response.Status.CREATED).entity(recipe).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, Recipe recipe) {
        return recipeService.update(id, recipe)
                .map(updated -> Response.ok(updated).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = recipeService.delete(id);
        if (deleted) {
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}