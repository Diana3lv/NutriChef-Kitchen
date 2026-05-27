package org.dsoft.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.dsoft.entity.dto.UserAdminDTO;
import org.dsoft.entity.model.RecipeStatus;
import org.dsoft.entity.model.User;
import org.dsoft.entity.model.UserRole;
import org.dsoft.repository.NutritionProfileRepository;
import org.dsoft.repository.RecipeFeedbackRepository;
import org.dsoft.repository.ShoppingListItemRepository;
import org.dsoft.repository.UserRecipeCollectionRepository;
import org.dsoft.repository.UserRecipeStatusRepository;
import org.dsoft.repository.UserRepository;

@ApplicationScoped
public class UserManagementService {

    @Inject
    UserRepository userRepository;

    @Inject
    ShoppingListItemRepository shoppingListItemRepository;

    @Inject
    UserRecipeStatusRepository userRecipeStatusRepository;

    @Inject
    UserRecipeCollectionRepository userRecipeCollectionRepository;

    @Inject
    NutritionProfileRepository nutritionProfileRepository;

    @Inject
    RecipeFeedbackRepository recipeFeedbackRepository;

    public List<UserAdminDTO> getAllUsers(String searchQuery, String roleFilter) {
        List<User> users;

        if (searchQuery != null && !searchQuery.isEmpty() && roleFilter != null && !roleFilter.isEmpty()) {
            // Search by email/name AND filter by role
            users = userRepository.find(
                "LOWER(email) LIKE LOWER(?1) OR LOWER(firstName) LIKE LOWER(?2) OR LOWER(lastName) LIKE LOWER(?3) AND role = ?4",
                "%" + searchQuery + "%",
                "%" + searchQuery + "%",
                "%" + searchQuery + "%",
                UserRole.valueOf(roleFilter)
            ).list();
        } else if (searchQuery != null && !searchQuery.isEmpty()) {
            // Search by email/name only
            users = userRepository.find(
                "LOWER(email) LIKE LOWER(?1) OR LOWER(firstName) LIKE LOWER(?2) OR LOWER(lastName) LIKE LOWER(?3)",
                "%" + searchQuery + "%",
                "%" + searchQuery + "%",
                "%" + searchQuery + "%"
            ).list();
        } else if (roleFilter != null && !roleFilter.isEmpty()) {
            // Filter by role only
            users = userRepository.find("role", UserRole.valueOf(roleFilter)).list();
        } else {
            // Get all users
            users = userRepository.listAll();
        }

        return users.stream()
            .map(this::mapToAdminDTO)
            .collect(Collectors.toList());
    }

    public UserAdminDTO getUserDetails(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        return mapToAdminDTO(user);
    }

    public UserAdminDTO updateUserRole(Long userId, String newRole) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        try {
            user.role = UserRole.valueOf(newRole);
            userRepository.persist(user);
            return mapToAdminDTO(user);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + newRole);
        }
    }

    public UserAdminDTO deactivateUser(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        user.isActive = false;
        user.updatedAt = LocalDateTime.now();
        userRepository.persist(user);
        return mapToAdminDTO(user);
    }

    public UserAdminDTO reactivateUser(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        user.isActive = true;
        user.updatedAt = LocalDateTime.now();
        userRepository.persist(user);
        return mapToAdminDTO(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        // Delete FK-constrained child records before removing the user
        shoppingListItemRepository.delete("user.id", userId);
        userRecipeStatusRepository.delete("user.id", userId);
        userRecipeCollectionRepository.delete("user.id", userId);
        recipeFeedbackRepository.delete("user.id", userId);
        nutritionProfileRepository.delete("user.id", userId);
        // Inventory cascades via User.inventory (CascadeType.ALL + orphanRemoval)
        userRepository.deleteById(userId);
    }

    public void updateLastLogin(Long userId) {
        User user = userRepository.findById(userId);
        if (user != null) {
            user.lastLogin = LocalDateTime.now();
            userRepository.persist(user);
        }
    }

    private UserAdminDTO mapToAdminDTO(User user) {
        long recipesDoneCount = userRecipeStatusRepository.count(
            "user.id = ?1 and status = ?2", user.id, RecipeStatus.DONE);
        return new UserAdminDTO(
            user.id,
            user.email,
            user.firstName,
            user.lastName,
            user.role != null ? user.role.name() : "USER",
            user.createdAt,
            user.lastLogin,
            user.isActive,
            recipesDoneCount
        );
    }
}
