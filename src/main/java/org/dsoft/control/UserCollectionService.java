package org.dsoft.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.dsoft.entity.dto.RecipeDTO;
import org.dsoft.entity.model.*;
import org.dsoft.repository.UserRecipeCollectionRepository;

import java.util.List;

@ApplicationScoped
public class UserCollectionService {

    @Inject
    UserRecipeCollectionRepository collectionRepository;

    @Inject
    UserService userService;

    @Inject
    RecipeService recipeService;

    public List<RecipeDTO> getCollection(Long userId, RecipeCollectionType type) {
        return collectionRepository.findByUserAndType(userId, type)
                .stream()
                .map(c -> c.recipe.toRecipeDTO())
                .toList();
    }

    public List<Long> getCollectionIds(Long userId, RecipeCollectionType type) {
        return collectionRepository.findRecipeIdsByUserAndType(userId, type);
    }

    @Transactional
    public void add(Long userId, Long recipeId, RecipeCollectionType type) {
        if (collectionRepository.existsByUserRecipeAndType(userId, recipeId, type)) {
            return;
        }
        User user = userService.getUserById(userId);
        Recipe recipe = recipeService.getById(recipeId)
                .orElseThrow(() -> new jakarta.ws.rs.NotFoundException("Recipe not found"));

        UserRecipeCollection entry = new UserRecipeCollection();
        entry.user = user;
        entry.recipe = recipe;
        entry.collectionType = type;
        collectionRepository.persist(entry);
    }

    @Transactional
    public void remove(Long userId, Long recipeId, RecipeCollectionType type) {
        collectionRepository.findByUserRecipeAndType(userId, recipeId, type)
                .ifPresent(collectionRepository::delete);
    }
}