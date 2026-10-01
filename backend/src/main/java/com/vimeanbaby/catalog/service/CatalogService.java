package com.vimeanbaby.catalog.service;

import com.vimeanbaby.catalog.dto.CatalogDtos.BrandRequest;
import com.vimeanbaby.catalog.dto.CatalogDtos.BrandResponse;
import com.vimeanbaby.catalog.dto.CatalogDtos.CategoryRequest;
import com.vimeanbaby.catalog.dto.CatalogDtos.CategoryResponse;
import com.vimeanbaby.catalog.dto.CatalogDtos.ProductDetailResponse;
import com.vimeanbaby.catalog.dto.CatalogDtos.ProductRequest;
import com.vimeanbaby.catalog.dto.CatalogDtos.ProductSummaryResponse;
import com.vimeanbaby.catalog.entity.Brand;
import com.vimeanbaby.catalog.entity.Category;
import com.vimeanbaby.catalog.entity.Product;
import com.vimeanbaby.catalog.entity.ProductImage;
import com.vimeanbaby.catalog.entity.ProductVariant;
import com.vimeanbaby.catalog.mapper.CatalogMapper;
import com.vimeanbaby.catalog.repository.BrandRepository;
import com.vimeanbaby.catalog.repository.CategoryRepository;
import com.vimeanbaby.catalog.repository.ProductRepository;
import com.vimeanbaby.common.PageResponse;
import com.vimeanbaby.exception.BadRequestException;
import com.vimeanbaby.exception.ResourceNotFoundException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogService {

    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final CatalogMapper catalogMapper;

    public List<CategoryResponse> listPublicCategories() {
        return categoryRepository.findByActiveTrueOrderBySortOrderAscIdAsc().stream()
                .map(catalogMapper::toCategoryResponse)
                .toList();
    }

    public List<BrandResponse> listPublicBrands() {
        return brandRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(catalogMapper::toBrandResponse)
                .toList();
    }

    public PageResponse<ProductSummaryResponse> searchProducts(
            String category,
            String brand,
            String q,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String age,
            String sort,
            int page,
            int size
    ) {
        Sort sortSpec = resolveSort(sort);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 48), sortSpec);

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("active")));
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (StringUtils.hasText(category)) {
                predicates.add(cb.equal(root.join("category").get("slug"), category.trim()));
            }
            if (StringUtils.hasText(brand)) {
                predicates.add(cb.equal(root.join("brand").get("slug"), brand.trim()));
            }
            if (StringUtils.hasText(q)) {
                String like = "%" + q.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("nameEn")), like),
                        cb.like(cb.lower(root.get("nameKm")), like),
                        cb.like(cb.lower(root.get("slug")), like)
                ));
            }
            if (StringUtils.hasText(age)) {
                predicates.add(cb.equal(root.get("ageRange"), age.trim()));
            }
            if (minPrice != null || maxPrice != null) {
                Join<Product, ProductVariant> variants = root.join("variants", JoinType.INNER);
                predicates.add(cb.isTrue(variants.get("active")));
                if (minPrice != null) {
                    predicates.add(cb.greaterThanOrEqualTo(variants.get("price"), minPrice));
                }
                if (maxPrice != null) {
                    predicates.add(cb.lessThanOrEqualTo(variants.get("price"), maxPrice));
                }
                if (query != null) {
                    query.distinct(true);
                }
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        Page<Product> result = productRepository.findAll(spec, pageable);
        List<ProductSummaryResponse> content = result.getContent().stream()
                .map(catalogMapper::toSummary)
                .toList();

        return new PageResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    public ProductDetailResponse getPublicProduct(String slug) {
        Product product = productRepository.findPublicBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + slug));
        return catalogMapper.toDetail(product);
    }

    public List<CategoryResponse> listAllCategories() {
        return categoryRepository.findAll(Sort.by("sortOrder").ascending().and(Sort.by("id").ascending()))
                .stream()
                .map(catalogMapper::toCategoryResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        validateCategorySlug(request.slug(), null);
        Category category = new Category();
        applyCategory(category, request);
        return catalogMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        validateCategorySlug(request.slug(), id);
        applyCategory(category, request);
        return catalogMapper.toCategoryResponse(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        category.setActive(false);
    }

    public List<BrandResponse> listAllBrands() {
        return brandRepository.findAll(Sort.by("name").ascending()).stream()
                .map(catalogMapper::toBrandResponse)
                .toList();
    }

    @Transactional
    public BrandResponse createBrand(BrandRequest request) {
        validateBrandSlug(request.slug(), null);
        Brand brand = new Brand();
        applyBrand(brand, request);
        return catalogMapper.toBrandResponse(brandRepository.save(brand));
    }

    @Transactional
    public BrandResponse updateBrand(Long id, BrandRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
        validateBrandSlug(request.slug(), id);
        applyBrand(brand, request);
        return catalogMapper.toBrandResponse(brand);
    }

    @Transactional
    public void deleteBrand(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
        brand.setActive(false);
    }

    public PageResponse<ProductSummaryResponse> listAdminProducts(
            String q,
            Long categoryId,
            Long brandId,
            String status,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by("id").descending());
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(q)) {
                String like = "%" + q.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("nameEn")), like),
                        cb.like(cb.lower(root.get("nameKm")), like),
                        cb.like(cb.lower(root.get("slug")), like)
                ));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (brandId != null) {
                predicates.add(cb.equal(root.get("brand").get("id"), brandId));
            }
            if ("active".equals(status)) {
                predicates.add(cb.isTrue(root.get("active")));
                predicates.add(cb.isNull(root.get("deletedAt")));
            } else if ("inactive".equals(status)) {
                predicates.add(cb.or(cb.isFalse(root.get("active")), cb.isNotNull(root.get("deletedAt"))));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        Page<Product> result = productRepository.findAll(spec, pageable);
        return new PageResponse<>(
                result.getContent().stream().map(catalogMapper::toSummary).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    public ProductDetailResponse getAdminProduct(Long id) {
        Product product = productRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return catalogMapper.toDetail(product, true);
    }

    @Transactional
    public ProductDetailResponse createProduct(ProductRequest request) {
        validateProductSlug(request.slug(), null);
        Product product = new Product();
        applyProduct(product, request);
        Product saved = productRepository.save(product);
        return catalogMapper.toDetail(productRepository.findDetailedById(saved.getId()).orElse(saved), true);
    }

    @Transactional
    public ProductDetailResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        validateProductSlug(request.slug(), id);
        product.getImages().clear();
        product.getVariants().clear();
        productRepository.flush();
        applyProduct(product, request);
        return catalogMapper.toDetail(product, true);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.setActive(false);
        product.setDeletedAt(Instant.now());
    }

    private void applyCategory(Category category, CategoryRequest request) {
        category.setNameKm(requireText(request.nameKm(), "nameKm"));
        category.setNameEn(requireText(request.nameEn(), "nameEn"));
        category.setSlug(requireText(request.slug(), "slug"));
        category.setImageUrl(request.imageUrl());
        category.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
        category.setActive(request.active() == null || request.active());
    }

    private void applyBrand(Brand brand, BrandRequest request) {
        brand.setName(requireText(request.name(), "name"));
        brand.setSlug(requireText(request.slug(), "slug"));
        brand.setLogoUrl(request.logoUrl());
        brand.setActive(request.active() == null || request.active());
    }

    private void applyProduct(Product product, ProductRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        Brand brand = null;
        if (request.brandId() != null) {
            brand = brandRepository.findById(request.brandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
        }

        product.setCategory(category);
        product.setBrand(brand);
        product.setNameKm(requireText(request.nameKm(), "nameKm"));
        product.setNameEn(requireText(request.nameEn(), "nameEn"));
        product.setSlug(requireText(request.slug(), "slug"));
        product.setDescriptionKm(request.descriptionKm());
        product.setDescriptionEn(request.descriptionEn());
        product.setAgeRange(request.ageRange());
        product.setOriginCountry(request.originCountry());
        product.setFeatured(Boolean.TRUE.equals(request.featured()));
        product.setActive(request.active() == null || request.active());
        product.setDeletedAt(null);

        if (request.images() != null) {
            int index = 0;
            for (var imageReq : request.images()) {
                ProductImage image = new ProductImage();
                image.setProduct(product);
                image.setUrl(requireText(imageReq.url(), "image.url"));
                image.setCloudinaryPublicId(imageReq.cloudinaryPublicId());
                image.setSortOrder(imageReq.sortOrder() != null ? imageReq.sortOrder() : index);
                image.setPrimary(Boolean.TRUE.equals(imageReq.primary()) || index == 0);
                product.getImages().add(image);
                index++;
            }
        }

        if (request.variants() == null || request.variants().isEmpty()) {
            throw new BadRequestException("At least one variant is required");
        }
        for (var variantReq : request.variants()) {
            ProductVariant variant = new ProductVariant();
            variant.setProduct(product);
            variant.setSku(requireText(variantReq.sku(), "variant.sku"));
            variant.setOptionName(requireText(variantReq.optionName(), "variant.optionName"));
            if (variantReq.price() == null || variantReq.price().signum() < 0) {
                throw new BadRequestException("Variant price must be >= 0");
            }
            variant.setPrice(variantReq.price());
            variant.setCompareAtPrice(variantReq.compareAtPrice());
            variant.setStockQty(variantReq.stockQty() != null ? variantReq.stockQty() : 0);
            variant.setExpiryDate(variantReq.expiryDate());
            variant.setActive(variantReq.active() == null || variantReq.active());
            product.getVariants().add(variant);
        }
    }

    private Sort resolveSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Order.desc("featured"), Sort.Order.desc("id"));
        }
        return switch (sort.trim()) {
            case "priceAsc" -> Sort.by("id").ascending();
            case "priceDesc" -> Sort.by("id").descending();
            case "nameAsc" -> Sort.by("nameEn").ascending();
            case "newest" -> Sort.by("id").descending();
            default -> Sort.by(Sort.Order.desc("featured"), Sort.Order.desc("id"));
        };
    }

    private void validateCategorySlug(String slug, Long id) {
        String value = requireText(slug, "slug");
        boolean exists = id == null
                ? categoryRepository.existsBySlug(value)
                : categoryRepository.existsBySlugAndIdNot(value, id);
        if (exists) {
            throw new BadRequestException("Category slug already exists");
        }
    }

    private void validateBrandSlug(String slug, Long id) {
        String value = requireText(slug, "slug");
        boolean exists = id == null
                ? brandRepository.existsBySlug(value)
                : brandRepository.existsBySlugAndIdNot(value, id);
        if (exists) {
            throw new BadRequestException("Brand slug already exists");
        }
    }

    private void validateProductSlug(String slug, Long id) {
        String value = requireText(slug, "slug");
        boolean exists = id == null
                ? productRepository.existsBySlug(value)
                : productRepository.existsBySlugAndIdNot(value, id);
        if (exists) {
            throw new BadRequestException("Product slug already exists");
        }
    }

    private String requireText(String value, String field) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(field + " is required");
        }
        return value.trim();
    }
}
