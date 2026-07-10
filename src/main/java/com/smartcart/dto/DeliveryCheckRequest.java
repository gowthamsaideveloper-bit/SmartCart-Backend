package com.smartcart.dto;

public class DeliveryCheckRequest {
    private Long productId;
    private String pincode;
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
}
