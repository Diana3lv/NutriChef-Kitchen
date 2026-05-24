package org.dsoft.entity.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_recipe_status",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"user_id", "recipe_id"},
        name = "uk_user_recipe_status"
    )
)
public class UserRecipeStatus extends PanacheEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    public Recipe recipe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public RecipeStatus status;

    @Column(name = "started_at")
    public LocalDateTime startedAt;

    @Column(name = "completed_at")
    public LocalDateTime completedAt;

    @Column(name = "cook_count", columnDefinition = "integer default 0")
    public int cookCount = 0;
}