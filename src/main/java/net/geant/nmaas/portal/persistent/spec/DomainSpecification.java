package net.geant.nmaas.portal.persistent.spec;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import net.geant.nmaas.portal.persistent.entity.Domain;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class DomainSpecification {

    public static Specification<Domain> containsTextInAttributes(String searchText, String... attributes) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }

        final String lowerCaseSearchText = "%" + searchText.toLowerCase() + "%";

        return (Root<Domain> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            for (String attribute : attributes) {
                if (attribute.equalsIgnoreCase("id")) {

                    predicates.add(cb.like(root.get(attribute).as(String.class), lowerCaseSearchText));
                } else {
                    predicates.add(cb.like(cb.lower(root.get(attribute)), lowerCaseSearchText));
                }
            }

            if (predicates.isEmpty()) {
                return cb.conjunction(); // Equivalent to true, no conditions
            } else {
                return cb.or(predicates.toArray(new Predicate[0]));
            }
        };
    }

}
