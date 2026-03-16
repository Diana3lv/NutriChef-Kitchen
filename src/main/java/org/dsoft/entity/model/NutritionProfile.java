package org.dsoft.entity.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "nutrition_profiles")
public class NutritionProfile extends PanacheEntity {

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @ElementCollection(targetClass = Allergen.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "nutrition_profile_allergens", joinColumns = @JoinColumn(name = "nutrition_profile_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "allergen", nullable = false)
    public List<Allergen> allergens;

    @ElementCollection(targetClass = DietaryPreference.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "nutrition_profile_dietary_preferences", joinColumns = @JoinColumn(name = "nutrition_profile_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "dietary_preference", nullable = false)
    public List<DietaryPreference> dietaryPreferences;

    @Column(columnDefinition = "TEXT")
    public String medicalConditions;
}
