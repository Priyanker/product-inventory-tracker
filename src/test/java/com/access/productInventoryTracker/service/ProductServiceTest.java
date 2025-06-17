package com.access.productInventoryTracker.service;

import com.access.productInventoryTracker.dto.ProductDTO;
import com.access.productInventoryTracker.model.Product;
import com.access.productInventoryTracker.repository.ProductRepository;

import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    public void setupMockProducts() {
        List<Product> mockProducts = Arrays.asList(
            new Product(1L, "Laptop", 1500.0, "Electronics", true),
            new Product(2L, "Smartphone", 800.0, "Electronics", false),
            new Product(3L, "Coffee Maker", 100.0, "Home Appliances", true),
            new Product(4L, "Blender", 150.0, "Home Appliances", true),
            new Product(5L, "T-Shirt", 30.0, "Apparel", true),
            new Product(6L, "Jeans", 45.0, "Apparel", true),
            new Product(7L, "Desk Lamp", 89.99, "Home Appliances", false),
            new Product(8L, "Wall Art", 120.0, "Home Decor", true),
            new Product(9L, "Sneakers", 75.0, "Apparel", true),
            new Product(10L, "Wristwatch", 250.0, "Accessories", false),
            new Product(11L, "Backpack", 60.0, "Accessories", true),
            new Product(12L, "Microwave Oven", 99.0, "Home Appliances", false),
            new Product(13L, "Floor Rug", 150.0, "Home Decor", true),
            new Product(14L, "Speaker", 300.0, "Electronics", true),
            new Product(15L, "E-reader", 200.0, "Electronics", false),
            new Product(16L, "Gaming Console", 499.99, "Electronics", true),
            new Product(17L, "Office Chair", 220.0, "Office Supplies", true),
            new Product(18L, "Pen Set", 29.99, "Office Supplies", true),
            new Product(19L, "Mountain Bike", 489.0, "Outdoor", true),
            new Product(20L, "Camping Tent", 270.0, "Outdoor", false)
        );

        when(productRepository.findAll()).thenReturn(mockProducts);
    }

    // Your tests here...
    @Test
    public void testCategoryCaseSensitivityBug() {
        List<ProductDTO> products = productService.getAllProducts();
    
        // Get any product with "Electronics" category from the mock data
        String originalCategory = "Electronics";
        
        boolean hasOriginalCase = products.stream()
            .anyMatch(p -> p.getCategory().equals(originalCategory));
        boolean hasLowerCase = products.stream()
            .anyMatch(p -> p.getCategory().equals(originalCategory.toLowerCase()));

        // Test should fail because original case is lost
        assertTrue(hasOriginalCase, "Should maintain original case 'Electronics'");
        assertFalse(hasLowerCase, "Should not convert to lowercase 'electronics'");
    }
    
    @Nested
    class PriceRangeFilterTests {
        @Test
        public void shouldFilterProductsWithinPriceRange() {
            List<ProductDTO> results = productService.filterByPriceRange(Optional.of(100.0), Optional.of(300.0));
            
            assertEquals(9, results.size(), "Should return 9 products in the range 100-300");
            assertTrue(results.stream().allMatch(p -> p.getPrice() >= 100.0 && p.getPrice() <= 300.0));
        }

        @Test
        public void shouldReturnEmptyListWhenNoProductsInRange() {
            List<ProductDTO> results = productService.filterByPriceRange(Optional.of(2000.0), Optional.of(3000.0));
            
            assertTrue(results.isEmpty(), "Should return empty list when no products in range");
        }

        @Test
        public void shouldFilterProductsWithMinPriceOnly() {
            List<ProductDTO> results = productService.filterByPriceRange(Optional.of(450.0), Optional.empty());
            
            assertEquals(4, results.size(), "Should return 4 products with price >= 450");
            assertTrue(results.stream().allMatch(p -> p.getPrice() >= 450.0));
        }

        @Test
        public void shouldFilterProductsWithMaxPriceOnly() {
            List<ProductDTO> results = productService.filterByPriceRange(Optional.empty(), Optional.of(50.0));
            
            assertEquals(3, results.size(), "Should return 3 products with price <= 50");
            assertTrue(results.stream().allMatch(p -> p.getPrice() <= 50.0));
        }

        @Test
        public void shouldReturnAllProductsWhenNoPriceFilters() {
            List<ProductDTO> results = productService.filterByPriceRange(Optional.empty(), Optional.empty());
            
            assertEquals(20, results.size(), "Should return all products when no filters are applied");
        }
    }

    @Nested
    class CategoryFilterTests {
        @Test
        public void shouldFilterByExistingCategory() {
            List<ProductDTO> results = productService.filterByCategory(Optional.of("Electronics"));
            
            assertEquals(5, results.size(), "Should return 5 products in 'Electronics' category");
            assertTrue(results.stream().allMatch(p -> p.getCategory().equals("Electronics")));
        }

        @Test
        public void shouldReturnEmptyListForNonexistentCategory() {
            List<ProductDTO> results = productService.filterByCategory(Optional.of("Books"));
            
            assertTrue(results.isEmpty(), "Should return empty list for non-existent category");
        }

        @Test
        public void shouldReturnEmptyListForEmptyCategory() {
            List<ProductDTO> results = productService.filterByCategory(Optional.of("  "));
            
            assertTrue(results.isEmpty(), "Should return empty list for blank category");
        }

        @Test
        public void shouldReturnEmptyListForNullCategory() {
            List<ProductDTO> results = productService.filterByCategory(Optional.empty());
            
            assertTrue(results.isEmpty(), "Should return empty list when no category is provided");
        }
    }

    @Nested
    class AvailabilityFilterTests {
        @Test
        public void shouldFilterAvailableProducts() {
            List<ProductDTO> results = productService.filterByAvailability(Optional.of(true));
            
            assertEquals(14, results.size(), "Should return 14 available products");
            assertTrue(results.stream().allMatch(ProductDTO::isAvailable));
        }

        @Test
        public void shouldFilterUnavailableProducts() {
            List<ProductDTO> results = productService.filterByAvailability(Optional.of(false));
            
            assertEquals(6, results.size(), "Should return 6 unavailable products");
            assertTrue(results.stream().allMatch(p -> !p.isAvailable()));
        }

        @Test
        public void shouldReturnAllProductsWhenNoAvailabilityFilter() {
            List<ProductDTO> results = productService.filterByAvailability(Optional.empty());
            
            assertEquals(20, results.size(), "Should return all products when no availability filter is applied");
        }
    }
}