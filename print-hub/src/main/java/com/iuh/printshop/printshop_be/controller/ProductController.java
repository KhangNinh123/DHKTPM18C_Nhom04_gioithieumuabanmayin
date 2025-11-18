package com.iuh.printshop.printshop_be.controller;

import com.iuh.printshop.printshop_be.dto.product.ProductRequest;
import com.iuh.printshop.printshop_be.dto.product.ProductResponse;
import com.iuh.printshop.printshop_be.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management APIs")
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @RequestPart("product") @Valid ProductRequest productRequest,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        if (image != null && !image.isEmpty()) {
            String imageUrl = productService.saveImage(image);
            productRequest.setImageUrl(imageUrl);
        }
        ProductResponse savedProduct = productService.createProduct(productRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Integer id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Integer id,
            @RequestPart("product") @Valid ProductRequest productRequest,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        if (image != null && !image.isEmpty()) {
            String imageUrl = productService.saveImage(image);
            productRequest.setImageUrl(imageUrl);
        }
        return productService.updateProduct(id, productRequest)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        if (productService.deleteProduct(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

