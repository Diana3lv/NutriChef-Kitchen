package org.dsoft.control;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dsoft.entity.model.IngredientSubstitution;
import org.dsoft.entity.model.Substitution;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class IngredientSubstitutionService {

    @Inject
    EntityManager em;

    @SuppressWarnings("null")
    public Map<String, List<Substitution>> getAllSubstitutions() {
        Map<String, List<Substitution>> map = new HashMap<>();

        List<IngredientSubstitution> substitutions = em.createQuery(
                "SELECT s FROM IngredientSubstitution s", IngredientSubstitution.class)
                .getResultList();

        for (IngredientSubstitution sub : substitutions) {
            if (sub.getAlternatives() != null) {
                map.put(sub.getPrimaryIngredient().toLowerCase(), sub.getAlternatives());
            }
        }

        return map;
    }

    @SuppressWarnings("null")
    public List<Substitution> getSubstitutions(String ingredient) {
        var result = em.createQuery(
                "SELECT s FROM IngredientSubstitution s WHERE LOWER(s.primaryIngredient) = LOWER(:ingredient)",
                IngredientSubstitution.class)
                .setParameter("ingredient", ingredient)
                .getResultList();

        if (result.isEmpty()) {
            return List.of();
        }
        
        IngredientSubstitution sub = result.get(0);
        return sub.getAlternatives() != null ? sub.getAlternatives() : List.of();
    }

    @Transactional
    public IngredientSubstitution createSubstitution(
            String primaryIngredient,
            List<Substitution> alternatives) {

        IngredientSubstitution sub = new IngredientSubstitution(primaryIngredient, alternatives, null);
        em.persist(sub);
        return sub;
    }

    @Transactional
    @SuppressWarnings("null")
    public void initializeDefaultSubstitutions() {
        // Check if already initialized
        long count = em.createQuery(
                "SELECT COUNT(s) FROM IngredientSubstitution s",
                Long.class)
                .getSingleResult();

        if (count > 0) {
            return;
        }

        // Milk alternatives
        createSubstitution("milk", List.of(
                new Substitution("almond milk", 1.0, "1:1 replacement"),
                new Substitution("oat milk", 1.0, "1:1 replacement"),
                new Substitution("soy milk", 1.0, "1:1 replacement"),
                new Substitution("coconut milk", 0.75, "use 3/4 amount")));

        // Sugar alternatives
        createSubstitution("sugar", List.of(
                new Substitution("honey", 0.75, "use 3/4 amount"),
                new Substitution("sweetener", 0.5, "use half amount"),
                new Substitution("maple syrup", 0.75, "use 3/4 amount")));

        // Egg alternatives
        createSubstitution("egg", List.of(
                new Substitution("applesauce", 0.25, "1/4 cup per egg"),
                new Substitution("banana", 0.25, "1/4 cup mashed per egg"),
                new Substitution("flax egg", 1.0, "1 tbsp ground flax + 3 tbsp water per egg")));

        // Butter alternatives
        createSubstitution("butter", List.of(
                new Substitution("olive oil", 0.75, "use 3/4 amount"),
                new Substitution("coconut oil", 1.0, "1:1 replacement"),
                new Substitution("applesauce", 1.0, "1:1 replacement")));

        // Chicken alternatives (for vegan/vegetarian)
        createSubstitution("chicken", List.of(
                new Substitution("tofu", 1.0, "firm tofu, 1:1"),
                new Substitution("chickpeas", 1.0, "1:1 weight replacement"),
                new Substitution("tempeh", 1.0, "1:1 replacement")));

        // Beef alternatives
        createSubstitution("beef", List.of(
                new Substitution("lentils", 1.0, "1:1 weight replacement"),
                new Substitution("mushrooms", 0.8, "use slightly less")));
    }
}
