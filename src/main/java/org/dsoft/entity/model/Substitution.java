package org.dsoft.entity.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Substitution {
    public String ingredient;
    public Double ratio;        // 1.0 = same amount, 0.5 = half
    public String notes;
}
