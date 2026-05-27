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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;
import org.dsoft.control.IngredientService;
import org.dsoft.control.SubstitutionLLMService;
import org.dsoft.entity.dto.IngredientDTO;
import org.dsoft.entity.dto.IngredientWithSubstitutionsDTO;
import org.dsoft.entity.dto.SubstitutionAlternativeInputDTO;
import org.dsoft.entity.dto.SubstitutionAlternativeResponseDTO;
import org.dsoft.entity.dto.SubstitutionDTO;
import org.dsoft.entity.dto.SubstitutionOptionResponseDTO;
import org.dsoft.entity.dto.AddSubstitutionRequestDTO;
import org.dsoft.entity.dto.UpdateIngredientRequestDTO;
import org.dsoft.entity.model.Ingredient;
import org.dsoft.entity.model.SubstitutionAlternative;
import org.dsoft.entity.model.SubstitutionOption;

@Path("/api/ingredients")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class IngredientController {

    @Inject
    IngredientService ingredientService;

    @Inject
    SubstitutionLLMService substitutionLLMService;

    @GET
    public List<IngredientWithSubstitutionsDTO> getAll() {
        return ingredientService.getAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{id}")
    public IngredientWithSubstitutionsDTO getById(@PathParam("id") Long id) {
        return ingredientService.getById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new NotFoundException("Ingredient not found"));
    }

    @GET
    @Path("/simple")
    public List<org.dsoft.entity.dto.SimpleIngredientDTO> getSimple() {
        return ingredientService.getAll().stream()
                .map(i -> new org.dsoft.entity.dto.SimpleIngredientDTO(i.id, i.name, i.unit, i.allergens))
                .collect(Collectors.toList());
    }

    private IngredientWithSubstitutionsDTO convertToDTO(Ingredient ingredient) {
        List<SubstitutionOptionResponseDTO> substitutionDTOs = ingredient.substitutions.stream()
                .map(this::convertSubstitutionOptionToDTO)
                .collect(Collectors.toList());

        IngredientWithSubstitutionsDTO dto = new IngredientWithSubstitutionsDTO(
                ingredient.id,
                ingredient.name,
                ingredient.unit,
                ingredient.allergens,
                substitutionDTOs
        );
        dto.setCategory(ingredient.category != null ? ingredient.category.getDisplayValue() : null);
        return dto;
    }

    private SubstitutionOptionResponseDTO convertSubstitutionOptionToDTO(SubstitutionOption option) {
        List<SubstitutionAlternativeResponseDTO> alternatives = option.alternatives.stream()
                .map(this::convertSubstitutionAlternativeToDTO)
                .collect(Collectors.toList());

        return new SubstitutionOptionResponseDTO(
                option.id,
                alternatives
        );
    }

    private SubstitutionAlternativeResponseDTO convertSubstitutionAlternativeToDTO(SubstitutionAlternative alternative) {
        Ingredient altIngredient = alternative.alternativeIngredient;
        return new SubstitutionAlternativeResponseDTO(
                altIngredient.id,
                altIngredient.name,
                altIngredient.unit,
                altIngredient.category != null ? altIngredient.category.getDisplayValue() : null,
                altIngredient.allergens,
                alternative.ratio,
                alternative.description,
                List.of()
        );
    }

    @POST
    @RolesAllowed("ADMIN")
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
        return Response.status(Response.Status.CREATED).entity(convertToDTO(created)).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response update(@PathParam("id") Long id, UpdateIngredientRequestDTO dto) {
        return ingredientService.update(id, dto)
                .map(updated -> Response.ok(convertToDTO(updated)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = ingredientService.delete(id);
        if (deleted) {
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("/{id}/substitutions")
    public Response getSubstitutions(@PathParam("id") Long id) {
        return ingredientService.getById(id)
                .map(ingredient -> {
                    List<SubstitutionDTO> substitutions = ingredientService.getSubstitutions(ingredient.name);
                    return Response.ok(substitutions).build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity("Ingredient not found")
                        .build());
    }

    @POST
    @Path("/{id}/substitutions")
    @RolesAllowed("ADMIN")
    public Response addSubstitutionOption(@PathParam("id") Long id, AddSubstitutionRequestDTO request) {
        if (request == null || request.getAlternatives() == null || request.getAlternatives().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("At least one alternative ingredient is required")
                    .build();
        }
        return ingredientService.addSubstitutionOption(id, request.getAlternatives())
                .map(dto -> Response.status(Response.Status.CREATED).entity(dto).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity("Ingredient not found")
                        .build());
    }

    @DELETE
    @Path("/{id}/substitutions/{substitutionId}")
    @RolesAllowed("ADMIN")
    public Response deleteSubstitutionOption(@PathParam("id") Long ingredientId, @PathParam("substitutionId") Long substitutionId) {
        return ingredientService.deleteSubstitutionOption(ingredientId, substitutionId)
                .map(dto -> Response.ok(dto).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity("Ingredient or substitution not found")
                        .build());
    }

    //to-do: this is just for testing, should be deleted
    @POST
    @Path("/test-substitutions")
    public Response testSubstitutions(IngredientDTO ingredientDTO) {
        if (ingredientDTO.getName() == null || ingredientDTO.getName().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Ingredient name is required")
                    .build();
        }

        List<IngredientDTO> substitutions = substitutionLLMService.findSubstitutions(ingredientDTO.getName());
        return Response.ok(substitutions).build();
    }
}
