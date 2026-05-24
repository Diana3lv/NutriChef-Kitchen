package org.dsoft.entity.dto;

import java.time.LocalDateTime;

public class UserRecipeStatusDTO {

    public Long recipeId;
    public String status;           // "IN_PROGRESS" | "DONE" | null 
    public LocalDateTime startedAt;
    public LocalDateTime completedAt;
    public RecipeFeedbackDTO feedback;
    public RecipeDTO recipe;        // full recipe data for displaying cards
    public int cookCount;

    public UserRecipeStatusDTO() {}
}