package org.dsoft.entity.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "substitution_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubstitutionOption extends PanacheEntity {

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "ingredient_id", nullable = false)
    public Ingredient ingredient;

    @OneToMany(mappedBy = "substitutionOption", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    public List<SubstitutionAlternative> alternatives = new ArrayList<>();
}
