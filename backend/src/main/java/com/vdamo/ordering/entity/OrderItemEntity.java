package com.vdamo.ordering.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("da_order_item")
public class OrderItemEntity extends BaseEntity {

    @TableId
    private Long id;
    private Long orderId;
    private Long storeId;
    private Long productId;
    private String itemName;
    private String itemCode;
    private Integer unitPriceInCent;
    private Integer quantity;
    private Integer originalAmountInCent;
    private Integer discountAmountInCent;
    private Integer payableAmountInCent;
    private String itemStatus;
    private Integer appendRound;
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public Integer getUnitPriceInCent() {
        return unitPriceInCent;
    }

    public void setUnitPriceInCent(Integer unitPriceInCent) {
        this.unitPriceInCent = unitPriceInCent;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getOriginalAmountInCent() {
        return originalAmountInCent;
    }

    public void setOriginalAmountInCent(Integer originalAmountInCent) {
        this.originalAmountInCent = originalAmountInCent;
    }

    public Integer getDiscountAmountInCent() {
        return discountAmountInCent;
    }

    public void setDiscountAmountInCent(Integer discountAmountInCent) {
        this.discountAmountInCent = discountAmountInCent;
    }

    public Integer getPayableAmountInCent() {
        return payableAmountInCent;
    }

    public void setPayableAmountInCent(Integer payableAmountInCent) {
        this.payableAmountInCent = payableAmountInCent;
    }

    public String getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(String itemStatus) {
        this.itemStatus = itemStatus;
    }

    public Integer getAppendRound() {
        return appendRound;
    }

    public void setAppendRound(Integer appendRound) {
        this.appendRound = appendRound;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
