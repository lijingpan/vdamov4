package com.vdamo.ordering.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("da_discount")
public class DiscountEntity extends BaseEntity {

    @TableId
    private Long id;
    private Long storeId;
    private String name;
    private String code;
    private String discountType;
    private String amountType;
    private Integer amountValue;
    private Integer thresholdAmountInCent;
    private Boolean stackable;
    private Boolean enabled;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String remark;

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

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public String getAmountType() {
        return amountType;
    }

    public void setAmountType(String amountType) {
        this.amountType = amountType;
    }

    public Integer getAmountValue() {
        return amountValue;
    }

    public void setAmountValue(Integer amountValue) {
        this.amountValue = amountValue;
    }

    public Integer getThresholdAmountInCent() {
        return thresholdAmountInCent;
    }

    public void setThresholdAmountInCent(Integer thresholdAmountInCent) {
        this.thresholdAmountInCent = thresholdAmountInCent;
    }

    public Boolean getStackable() {
        return stackable;
    }

    public void setStackable(Boolean stackable) {
        this.stackable = stackable;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
