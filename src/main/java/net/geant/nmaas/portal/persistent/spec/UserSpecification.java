package net.geant.nmaas.portal.persistent.spec;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import net.geant.nmaas.portal.persistent.entity.User;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;


public class UserSpecification {


    public static Specification<User> findBySearchValue(String searchValue) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            query.distinct(true);

            if (searchValue != null && !searchValue.trim().isEmpty()) {
                String lowerCaseSearchValue = searchValue.toLowerCase().trim();

                if (lowerCaseSearchValue.matches("\\d+")) {
                    predicates.add(criteriaBuilder.equal(root.get("id"), Long.valueOf(lowerCaseSearchValue)));
                }

                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("username")),
                        "%" + lowerCaseSearchValue + "%"));


                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("firstname")),
                        "%" + lowerCaseSearchValue + "%"));


                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("lastname")),
                        "%" + lowerCaseSearchValue + "%"));

                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")),
                        "%" + lowerCaseSearchValue + "%"));


                return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
            }

            return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
        };
    }
}
