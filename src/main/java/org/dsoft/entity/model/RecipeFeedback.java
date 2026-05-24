package org.dsoft.entity.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "recipe_feedback",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"user_id", "recipe_id"},
        name = "uk_user_recipe_feedback"
    )
)
public class RecipeFeedback extends PanacheEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    public Recipe recipe;

    @Column(nullable = false)
    public int rating;

    @Column(name = "liked_notes", columnDefinition = "TEXT")
    public String likedNotes;

    @Column(name = "improvement_notes", columnDefinition = "TEXT")
    public String improvementNotes;

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
}