package com.smartcart.controller;

import com.smartcart.dto.ApiResponse;
import com.smartcart.model.Product;
import com.smartcart.model.ProductImage;
import com.smartcart.model.Review;
import com.smartcart.repository.ProductImageRepository;
import com.smartcart.repository.ProductRepository;
import com.smartcart.repository.ReviewRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ReviewRepository reviewRepository;

    public ProductController(ProductRepository productRepository, ProductImageRepository productImageRepository, ReviewRepository reviewRepository) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Object>> all(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        List<Product> products = categoryId == null ? productRepository.findAll() : productRepository.findByCategoryId(categoryId);
        products = products.stream().filter(p -> {
            boolean ok = true;
            if (categoryName != null && !categoryName.isBlank()) {
                ok = p.getCategory() != null && p.getCategory().getName() != null && p.getCategory().getName().equalsIgnoreCase(categoryName);
            }
            if (ok && q != null && !q.isBlank()) {
                String text = (safe(p.getName()) + " " + safe(p.getDescription()) + " " + safe(p.getBrand()) + " " + (p.getCategory() == null ? "" : safe(p.getCategory().getName())) + " " + safe(p.getProductType())).toLowerCase();
                ok = text.contains(q.toLowerCase());
            }
            if (ok && brand != null && !brand.isBlank()) ok = p.getBrand() != null && p.getBrand().equalsIgnoreCase(brand);
            if (ok && minPrice != null) ok = p.getPrice() != null && p.getPrice().compareTo(minPrice) >= 0;
            if (ok && maxPrice != null) ok = p.getPrice() != null && p.getPrice().compareTo(maxPrice) <= 0;
            if (ok && inStock != null && inStock) ok = p.getStock() != null && p.getStock() > 0;
            return ok;
        }).collect(Collectors.toList());

        if ("price_low".equalsIgnoreCase(sort)) products.sort(Comparator.comparing(p -> p.getPrice() == null ? BigDecimal.ZERO : p.getPrice()));
        if ("price_high".equalsIgnoreCase(sort)) products.sort(Comparator.comparing((Product p) -> p.getPrice() == null ? BigDecimal.ZERO : p.getPrice()).reversed());
        if ("rating".equalsIgnoreCase(sort)) products.sort(Comparator.comparing((Product p) -> p.getRating() == null ? 0.0 : p.getRating()).reversed());
        if ("newest".equalsIgnoreCase(sort)) products.sort(Comparator.comparing(Product::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));

        if (page != null || size != null) {
            int pageNumber = page == null || page < 0 ? 0 : page;
            int pageSize = size == null || size <= 0 ? 12 : Math.min(size, 50);
            int total = products.size();
            int from = Math.min(pageNumber * pageSize, total);
            int to = Math.min(from + pageSize, total);
            List<Product> items = products.subList(from, to);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("items", items);
            data.put("page", pageNumber);
            data.put("size", pageSize);
            data.put("total", total);
            data.put("totalPages", (int) Math.ceil(total / (double) pageSize));
            data.put("hasMore", to < total);
            return ResponseEntity.ok(ApiResponse.ok("Products fetched page by page", (Object) data));
        }

        return ResponseEntity.ok(ApiResponse.ok("Products fetched", (Object) products));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<Product>>> featured() {
        return ResponseEntity.ok(ApiResponse.ok("Featured products fetched", productRepository.findByFeaturedTrue()));
    }

    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<List<Product>>> trending() {
        return ResponseEntity.ok(ApiResponse.ok("Trending products fetched", productRepository.findByTrendingTrue()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> get(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(product -> ResponseEntity.ok(ApiResponse.ok("Product fetched", product)))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.fail("Product not found")));
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<ApiResponse<List<ProductImage>>> images(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Product images fetched", productImageRepository.findByProductIdOrderBySortOrderAsc(id)));
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<ApiResponse<List<Review>>> reviews(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Reviews fetched", reviewRepository.findByProductIdOrderByCreatedAtDesc(id)));
    }

    @GetMapping("/{id}/related")
    public ResponseEntity<ApiResponse<List<Product>>> related(@PathVariable Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null || product.getCategory() == null) return ResponseEntity.ok(ApiResponse.ok("Related products fetched", List.of()));
        List<Product> related = productRepository.findByCategoryId(product.getCategory().getId()).stream()
                .filter(p -> !p.getId().equals(id)).limit(6).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok("Related products fetched", related));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Product>>> search(@RequestParam String keyword) {
        List<Product> products = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
        return ResponseEntity.ok(ApiResponse.ok("Search completed", products));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
