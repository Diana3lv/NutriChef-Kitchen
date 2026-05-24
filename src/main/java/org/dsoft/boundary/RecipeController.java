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
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.dsoft.control.RecipeService;
import org.dsoft.entity.dto.CreateRecipeRequestDTO;
import org.dsoft.entity.dto.RecipeDTO;
import org.dsoft.entity.model.Recipe;
import org.dsoft.entity.model.RecipeTag;
import jakarta.ws.rs.QueryParam;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Path("/recipes")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RecipeController {

    @Inject
    RecipeService recipeService;

    @GET
    public List<RecipeDTO> getAll() {
        return recipeService.getAll().stream()
                .map(Recipe::toRecipeDTO)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/search")
    public List<RecipeDTO> search(@QueryParam("q") String query) {
        return recipeService.search(query).stream()
                .map(Recipe::toRecipeDTO)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/personalized")
    @RolesAllowed({"USER", "ADMIN"})
    public List<RecipeDTO> getPersonalizedRecipes(@Context SecurityContext securityContext) {
        Long userId = Long.parseLong(securityContext.getUserPrincipal().getName());
        return recipeService.getRecipesForUserNutritionProfile(userId).stream()
                .map(Recipe::toRecipeDTO)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{id}")
    public RecipeDTO getById(@PathParam("id") Long id) {
        return recipeService.getById(id)
                .map(Recipe::toRecipeDTO)
                .orElseThrow(() -> new NotFoundException("Recipe not found"));
    }

    @POST
    @RolesAllowed("ADMIN")
    public Response create(CreateRecipeRequestDTO request) {
        Recipe recipe = recipeService.createFromRequest(request);
        return Response.status(Response.Status.CREATED).entity(recipe.toRecipeDTO()).build();
    }

    @POST
    @Path("/images")
    @RolesAllowed("ADMIN")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadImage(@RestForm("image") FileUpload file) {
        if (file == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "No image provided")).build();
        }
        String imageUrl = recipeService.saveImage(file);
        return Response.ok(Map.of("imageUrl", imageUrl)).build();
    }

    @GET
    @Path("/images/{filename}")
    @Produces(MediaType.WILDCARD)
    public Response getImage(@PathParam("filename") String filename) {
        return recipeService.getImagePath(filename)
                .map(path -> {
                    try {
                        byte[] data = Files.readAllBytes(path);
                        String contentType = Files.probeContentType(path);
                        if (contentType == null) contentType = MediaType.APPLICATION_OCTET_STREAM;
                        return Response.ok(data).type(contentType).build();
                    } catch (IOException e) {
                        return Response.serverError().build();
                    }
                })
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response update(@PathParam("id") Long id, CreateRecipeRequestDTO request) {
        return recipeService.updateFromRequest(id, request)
                .map(updated -> Response.ok(updated.toRecipeDTO()).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = recipeService.delete(id);
        if (deleted) {
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("/tag/{tag}")
    public List<RecipeDTO> getByTag(@PathParam("tag") String tag) {
        try {
            RecipeTag recipeTag = RecipeTag.valueOf(tag.toUpperCase());
            return recipeService.getByTag(recipeTag).stream()
                    .map(Recipe::toRecipeDTO)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Unknown tag: " + tag);
        }
    }
}