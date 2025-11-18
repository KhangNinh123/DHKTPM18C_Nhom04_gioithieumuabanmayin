package com.iuh.printshop.printshop_be.specification;

import com.iuh.printshop.printshop_be.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> searchProducts(
            String keyword,
            Integer categoryId,
            Integer brandId,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search by keyword (name or description)
            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchPattern = "%" + keyword.toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")), searchPattern
                );
                Predicate descriptionPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), searchPattern
                );
                predicates.add(criteriaBuilder.or(namePredicate, descriptionPredicate));
            }

            // Filter by category
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("category").get("id"), categoryId
                ));
            }

            // Filter by brand
            if (brandId != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("brand").get("id"), brandId
                ));
            }

            // Filter by price range
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("price"), minPrice
                ));
            }

            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("price"), maxPrice
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

