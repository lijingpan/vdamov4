package com.vdamo.ordering.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("da_order")
public class OrderEntity extends BaseEntity {

    @TableId
    private Long id;
    private String orderNo;
    private Long storeId;
    private Long tableId;
    private Long memberId;
    private Integer originalAmountInCent;
    private Integer discountAmountInCent;
    private Integer payableAmountInCent;
    private Integer paidAmountInCent;
    private String orderStatus;
    private String paymentStatus;
    private Integer appendCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
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

    public Integer getPaidAmountInCent() {
        return paidAmountInCent;
    }

    public void setPaidAmountInCent(Integer paidAmountInCent) {
        this.paidAmountInCent = paidAmountInCent;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Integer getAppendCount() {
        return appendCount;
    }

    public void setAppendCount(Integer appendCount) {
        this.appendCount = appendCount;
    }
}
