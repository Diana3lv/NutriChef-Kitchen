package org.dsoft.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import org.dsoft.entity.model.NutritionProfile;
import org.dsoft.entity.model.User;

@ApplicationScoped
public class NutritionProfileRepository implements PanacheRepository<NutritionProfile> {

    public Optional<NutritionProfile> findByUser(User user) {
        return find("user", user).firstResultOptional();
    }
}
