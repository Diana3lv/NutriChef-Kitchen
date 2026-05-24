package org.dsoft.entity.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_recipe_collections",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "recipe_id", "collection_type"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRecipeCollection extends PanacheEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    public Recipe recipe;

    @Enumerated(EnumType.STRING)
    @Column(name = "collection_type", nullable = false)
    public RecipeCollectionType collectionType;

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}