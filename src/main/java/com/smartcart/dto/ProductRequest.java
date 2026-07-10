package com.smartcart.dto;

import java.math.BigDecimal;

public class ProductRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal mrp;
    private Integer discountPercent;
    private Integer stock;
    private String imageUrl;
    private Long categoryId;
    private String brand;
    private String sizeChart;
    private String color;
    private Double rating;
    private Integer reviewCount;
    private String styleTag;
    private String demandTag;
    private String seasonTag;
    private String productType;
    private String availablePincodes;
    private BigDecimal deliveryCharge;
    private Integer estimatedDeliveryDays;
    private Boolean trending;
    private Boolean featured;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getMrp() { return mrp; }
    public void setMrp(BigDecimal mrp) { this.mrp = mrp; }
    public Integer getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(Integer discountPercent) { this.discountPercent = discountPercent; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getSizeChart() { return sizeChart; }
    public void setSizeChart(String sizeChart) { this.sizeChart = sizeChart; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
    public String getStyleTag() { return styleTag; }
    public void setStyleTag(String styleTag) { this.styleTag = styleTag; }
    public String getDemandTag() { return demandTag; }
    public void setDemandTag(String demandTag) { this.demandTag = demandTag; }
    public String getSeasonTag() { return seasonTag; }
    public void setSeasonTag(String seasonTag) { this.seasonTag = seasonTag; }
    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }
    public String getAvailablePincodes() { return availablePincodes; }
    public void setAvailablePincodes(String availablePincodes) { this.availablePincodes = availablePincodes; }
    public BigDecimal getDeliveryCharge() { return deliveryCharge; }
    public void setDeliveryCharge(BigDecimal deliveryCharge) { this.deliveryCharge = deliveryCharge; }
    public Integer getEstimatedDeliveryDays() { return estimatedDeliveryDays; }
    public void setEstimatedDeliveryDays(Integer estimatedDeliveryDays) { this.estimatedDeliveryDays = estimatedDeliveryDays; }
    public Boolean getTrending() { return trending; }
    public void setTrending(Boolean trending) { this.trending = trending; }
    public Boolean getFeatured() { return featured; }
    public void setFeatured(Boolean featured) { this.featured = featured; }
}
