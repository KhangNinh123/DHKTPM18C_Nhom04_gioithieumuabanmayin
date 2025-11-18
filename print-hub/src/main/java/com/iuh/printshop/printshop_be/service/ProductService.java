package com.iuh.printshop.printshop_be.service;

import com.iuh.printshop.printshop_be.dto.product.ProductPageResponse;
import com.iuh.printshop.printshop_be.dto.product.ProductRequest;
import com.iuh.printshop.printshop_be.dto.product.ProductResponse;
import com.iuh.printshop.printshop_be.dto.product.ProductSearchRequest;
import com.iuh.printshop.printshop_be.entity.Brand;
import com.iuh.printshop.printshop_be.entity.Category;
import com.iuh.printshop.printshop_be.entity.Product;
import com.iuh.printshop.printshop_be.repository.BrandRepository;
import com.iuh.printshop.printshop_be.repository.CategoryRepository;
import com.iuh.printshop.printshop_be.repository.ProductRepository;
import com.iuh.printshop.printshop_be.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String saveImage(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Files.copy(file.getInputStream(), uploadPath.resolve(fileName));
        return "/uploads/" + fileName;
    }

    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new RuntimeException("Brand not found"));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .category(category)
                .brand(brand)
                .build();
        return convertToDto(productRepository.save(product));
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<ProductResponse> getProductById(Integer id) {
        return productRepository.findById(id).map(this::convertToDto);
    }

    public Optional<ProductResponse> updateProduct(Integer id, ProductRequest request) {
        return productRepository.findById(id).map(product -> {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new RuntimeException("Brand not found"));

            product.setName(request.getName());
            product.setDescription(request.getDescription());
            product.setPrice(request.getPrice());
            product.setStockQuantity(request.getStockQuantity());
            product.setImageUrl(request.getImageUrl());
            product.setCategory(category);
            product.setBrand(brand);

            return convertToDto(productRepository.save(product));
        });
    }

    public boolean deleteProduct(Integer id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public ProductPageResponse searchProducts(ProductSearchRequest request) {
        // Build specification for filtering
        Specification<Product> spec = ProductSpecification.searchProducts(
            request.getKeyword(),
            request.getCategoryId(),
            request.getBrandId(),
            request.getMinPrice(),
            request.getMaxPrice()
        );

        // Build sort
        Sort sort = buildSort(request.getSortBy());

        // Build pagination
        Pageable pageable = PageRequest.of(
            request.getPage() != null ? request.getPage() : 0,
            request.getSize() != null ? request.getSize() : 20,
            sort
        );

        // Execute query
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        // Convert to DTO
        List<ProductResponse> content = productPage.getContent().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());

        return ProductPageResponse.builder()
            .content(content)
            .page(productPage.getNumber())
            .size(productPage.getSize())
            .totalElements(productPage.getTotalElements())
            .totalPages(productPage.getTotalPages())
            .first(productPage.isFirst())
            .last(productPage.isLast())
            .build();
    }

    private Sort buildSort(String sortBy) {
        if (sortBy == null || sortBy.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt"); // Default: newest first
        }

        switch (sortBy.toLowerCase()) {
            case "price_asc":
                return Sort.by(Sort.Direction.ASC, "price");
            case "price_desc":
                return Sort.by(Sort.Direction.DESC, "price");
            case "name_asc":
                return Sort.by(Sort.Direction.ASC, "name");
            case "name_desc":
                return Sort.by(Sort.Direction.DESC, "name");
            case "newest":
                return Sort.by(Sort.Direction.DESC, "createdAt");
            case "oldest":
                return Sort.by(Sort.Direction.ASC, "createdAt");
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }

    private ProductResponse convertToDto(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .brandId(product.getBrand() != null ? product.getBrand().getId() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

//    phan trang
    public Page<Product> getProducts(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findAll(pageable);
    }

//    Sort dong theo tung tieu chi
    public Page<Product> sortBy (int page, int size, String sortBy, String direction){
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findAll(pageable);
    }

    public Page<Product> filterProduct(
            String keyword,
            int categoryId,
            int brandId,
            double minPrice,
            double maxPrice,
            boolean inStock,
            int page,
            int size,
            String sortBy,
            String direction
    ){
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> result = productRepository.filterProducts(
                keyword, categoryId, brandId, minPrice, maxPrice, inStock, pageable
        );

        return result;
    }
}

