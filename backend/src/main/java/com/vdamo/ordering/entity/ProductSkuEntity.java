package com.vdamo.ordering.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("da_product_sku")
public class ProductSkuEntity extends BaseEntity {

    @TableId
    private Long id;
    private Long productId;
    private Long storeId;
    private String skuCode;
    private String barcode;
    private String specKey;
    private String specName;
    private Integer priceInCent;
    private Integer linePriceInCent;
    private Integer costPriceInCent;
    private Integer boxFeeInCent;
    private Integer stockQty;
    private Boolean autoReplenish;
    private Integer weightUnitGram;
    private Integer sortOrder;
    private Boolean active;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getSpecKey() {
        return specKey;
    }

    public void setSpecKey(String specKey) {
        this.specKey = specKey;
    }

    public String getSpecName() {
        return specName;
    }

    public void setSpecName(String specName) {
        this.specName = specName;
    }

    public Integer getPriceInCent() {
        return priceInCent;
    }

    public void setPriceInCent(Integer priceInCent) {
        this.priceInCent = priceInCent;
    }

    public Integer getLinePriceInCent() {
        return linePriceInCent;
    }

    public void setLinePriceInCent(Integer linePriceInCent) {
        this.linePriceInCent = linePriceInCent;
    }

    public Integer getCostPriceInCent() {
        return costPriceInCent;
    }

    public void setCostPriceInCent(Integer costPriceInCent) {
        this.costPriceInCent = costPriceInCent;
    }

    public Integer getBoxFeeInCent() {
        return boxFeeInCent;
    }

    public void setBoxFeeInCent(Integer boxFeeInCent) {
        this.boxFeeInCent = boxFeeInCent;
    }

    public Integer getStockQty() {
        return stockQty;
    }

    public void setStockQty(Integer stockQty) {
        this.stockQty = stockQty;
    }

    public Boolean getAutoReplenish() {
        return autoReplenish;
    }

    public void setAutoReplenish(Boolean autoReplenish) {
        this.autoReplenish = autoReplenish;
    }

    public Integer getWeightUnitGram() {
        return weightUnitGram;
    }

    public void setWeightUnitGram(Integer weightUnitGram) {
        this.weightUnitGram = weightUnitGram;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
