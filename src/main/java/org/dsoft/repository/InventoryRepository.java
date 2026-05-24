package org.dsoft.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.dsoft.entity.model.Inventory;
import java.util.Optional;

@ApplicationScoped
public class InventoryRepository implements PanacheRepository<Inventory> {

    public Optional<Inventory> findByUserId(Long userId) {
        return find("user.id", userId).firstResultOptional();
    }
}
