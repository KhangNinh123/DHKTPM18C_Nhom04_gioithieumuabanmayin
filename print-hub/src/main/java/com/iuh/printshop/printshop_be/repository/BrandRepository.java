package com.iuh.printshop.printshop_be.repository;

import com.iuh.printshop.printshop_be.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Integer> {
    Optional<Brand> findByName(String name);

    @Query("""
            SELECT b FROM Brand b
            WHERE (:keyword IS NULL 
                OR LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Brand> searchBrands(@Param("keyword") String keyword, Pageable pageable);
}

