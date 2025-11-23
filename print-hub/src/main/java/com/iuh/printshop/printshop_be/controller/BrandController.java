package com.iuh.printshop.printshop_be.controller;

import com.iuh.printshop.printshop_be.entity.Brand;
import com.iuh.printshop.printshop_be.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @PostMapping
    public ResponseEntity<?> createBrand(@Valid @RequestBody Brand brand, BindingResult br) {

        if (br.hasErrors()) {
            return ResponseEntity.badRequest().body(getValidationErrors(br));
        }

        try {
            Brand savedBrand = brandService.createBrand(brand);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedBrand);

        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(
                    Map.of("errors", Map.of("name", ex.getMessage()))
            );
        }
    }

    @GetMapping
    public ResponseEntity<List<Brand>> getAllBrands() {
        return ResponseEntity.ok(brandService.getAllBrands());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Brand> getBrandById(@PathVariable Integer id) {
        return brandService.getBrandById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBrand(@PathVariable Integer id,
                                         @Valid @RequestBody Brand brand,
                                         BindingResult bindingResult) {

        // Validation lỗi → trả về JSON error
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(getValidationErrors(bindingResult));
        }

        return brandService.updateBrand(id, brand)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrand(@PathVariable Integer id) {
        if (brandService.deleteBrand(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private Map<String, Object> getValidationErrors(BindingResult bindingResult) {
        Map<String, String> fieldErrors = new HashMap<>();

        for (FieldError error : bindingResult.getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("errors", fieldErrors);

        return response;
    }
}
