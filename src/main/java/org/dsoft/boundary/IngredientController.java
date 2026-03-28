package org.dsoft.boundary;

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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.dsoft.control.IngredientService;
import org.dsoft.entity.model.Ingredient;

@Path("/api/ingredients")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class IngredientController {

    @Inject
    IngredientService ingredientService;

    @GET
    public List<Ingredient> getAll() {
        return ingredientService.getAll();
    }

    @GET
    @Path("/{id}")
    public Ingredient getById(@PathParam("id") Long id) {
        Ingredient ingredient = ingredientService.getById(id);
        if (ingredient == null) {
            throw new NotFoundException("Ingredient not found");
        }
        return ingredient;
    }

    @POST
    public Response create(Ingredient ingredient) {
        if (ingredient.name == null || ingredient.name.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Ingredient name is required")
                    .build();
        }
        
        if (ingredient.unit == null || ingredient.unit.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Ingredient unit is required")
                    .build();
        }
        
        Ingredient created = ingredientService.create(ingredient);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, Ingredient ingredient) {
        Ingredient updated = ingredientService.update(id, ingredient);
        if (updated == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = ingredientService.delete(id);
        if (deleted) {
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
