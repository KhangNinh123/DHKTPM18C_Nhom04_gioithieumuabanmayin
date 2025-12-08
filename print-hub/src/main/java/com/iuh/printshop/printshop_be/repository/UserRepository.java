package com.iuh.printshop.printshop_be.repository;

import com.iuh.printshop.printshop_be.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    @Query("""
            SELECT DISTINCT u FROM User u
            LEFT JOIN u.roles r
            WHERE
                (:keyword IS NULL 
                    OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(u.email)    LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(u.phone)    LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
            AND (:role IS NULL OR r.name = :role)
            AND (:isActive IS NULL OR u.isActive = :isActive)
            """)
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("role") String role,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );
}
