package com.vdamo.ordering.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("da_product")
public class ProductEntity extends BaseEntity {

    @TableId
    private Long id;
    private Long storeId;
    private String name;
    private String code;
    private String categoryCode;
    private String productType;
    private String specMode;
    private Integer priceInCent;
    private String description;
    private Boolean attrEnabled;
    private Boolean materialEnabled;
    private Boolean weighedEnabled;
    private Boolean active;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getSpecMode() {
        return specMode;
    }

    public void setSpecMode(String specMode) {
        this.specMode = specMode;
    }

    public Integer getPriceInCent() {
        return priceInCent;
    }

    public void setPriceInCent(Integer priceInCent) {
        this.priceInCent = priceInCent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getAttrEnabled() {
        return attrEnabled;
    }

    public void setAttrEnabled(Boolean attrEnabled) {
        this.attrEnabled = attrEnabled;
    }

    public Boolean getMaterialEnabled() {
        return materialEnabled;
    }

    public void setMaterialEnabled(Boolean materialEnabled) {
        this.materialEnabled = materialEnabled;
    }

    public Boolean getWeighedEnabled() {
        return weighedEnabled;
    }

    public void setWeighedEnabled(Boolean weighedEnabled) {
        this.weighedEnabled = weighedEnabled;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
