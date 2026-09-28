package com.vimeanbaby.catalog.entity;

import com.vimeanbaby.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Column(name = "name_km", nullable = false)
    private String nameKm;

    @Column(name = "name_en", nullable = false)
    private String nameEn;

    @Column(nullable = false, unique = true, length = 220)
    private String slug;

    @Column(name = "description_km", columnDefinition = "TEXT")
    private String descriptionKm;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "age_range", length = 50)
    private String ageRange;

    @Column(name = "origin_country", length = 100)
    private String originCountry;

    @Column(name = "is_featured", nullable = false)
    private Boolean featured = false;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ProductVariant> variants = new ArrayList<>();
}
