package org.dsoft.entity.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ingredients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ingredient extends PanacheEntity {

    @Column(nullable = false, unique = true)
    public String name;

    @Column(nullable = false)
    public String unit;

    @Column
    public String category;

    @Column(name = "food_group")
    public String foodGroup;

    @ElementCollection(targetClass = Allergen.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    public List<Allergen> allergens = new ArrayList<>();

    @OneToMany(mappedBy = "ingredient", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    public List<SubstitutionOption> substitutions = new ArrayList<>();
}
