package org.dsoft.boundary;

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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.dsoft.control.RecipeService;
import org.dsoft.entity.model.Recipe;

@Path("/recipes")
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
    @Path("/{id}")
    public Recipe getById(@PathParam("id") Long id) {
        Recipe recipe = recipeService.getById(id);
        if (recipe == null) {
            throw new NotFoundException("Recipe not found");
        }
        return recipe;
    }

    @POST
    public Response create(Recipe recipe) {
        recipeService.create(recipe);
        return Response.status(Response.Status.CREATED).entity(recipe).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, Recipe recipe) {
        Recipe updated = recipeService.update(id, recipe);
        if (updated == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(updated).build();
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