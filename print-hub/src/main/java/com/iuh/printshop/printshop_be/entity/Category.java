package com.iuh.printshop.printshop_be.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Category name cannot be empty")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Size(max = 500, message = "Category description must not exceed 500 characters")
    @Column(length = 500)
    private String description;
}

