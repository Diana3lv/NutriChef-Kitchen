package org.dsoft.entity.model;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubstitutionGroup {
    public Object substitution;  // Long sau List<Long>

    public SubstitutionGroup(Long id) {
        this.substitution = id;
    }

    public SubstitutionGroup(List<Long> ids) {
        this.substitution = ids;
    }
}

