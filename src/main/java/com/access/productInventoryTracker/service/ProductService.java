package com.access.productInventoryTracker.service;

import com.access.productInventoryTracker.dto.ProductDTO;
import com.access.productInventoryTracker.model.Product;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.access.productInventoryTracker.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    // Helper method to convert Product to ProductDTO
    private ProductDTO convertToDTO(Product product) {
        return new ProductDTO(
            product.getId(),
            product.getName(),
            product.getPrice(),
            product.getCategory(),
            product.isAvailable()
        );
    }
    
    // Get all products as DTOs
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    // Your filtering methods here...

    // Defining a functional interface for our filters
    @FunctionalInterface
    private interface ProductPredicate {
        boolean test(Product product);
    }

    // This is a common filtering method to reduce code duplication. It takes a predicate and returns filtered DTOs
    // Uses streams to get all products from repository, apply the provided filter condition and convert filtered products to DTOs
    private List<ProductDTO> filterProducts(ProductPredicate predicate) {
        return productRepository.findAll().stream()
            .filter(predicate::test)
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    // Filter products within a price range
    public List<ProductDTO> filterByPriceRange(Optional<Double> minPrice, Optional<Double> maxPrice) {
        ProductPredicate priceFilter = product -> 
            minPrice.map(min -> product.getPrice() >= min).orElse(true) &&
            maxPrice.map(max -> product.getPrice() <= max).orElse(true);
        
        return filterProducts(priceFilter);
    }

    // Filter products by their category
    public List<ProductDTO> filterByCategory(Optional<String> category) {
        return category
            .filter(cat -> !cat.trim().isEmpty())
            .map(cat -> filterProducts(product -> product.getCategory().equals(cat)))
            .orElse(Collections.emptyList());
    }

    // Filter products by availability status
    public List<ProductDTO> filterByAvailability(Optional<Boolean> availabilityStatus) {
        ProductPredicate availabilityFilter = product ->
            availabilityStatus.map(status -> product.isAvailable() == status)
                .orElse(true);
        
        return filterProducts(availabilityFilter);
    }
}
