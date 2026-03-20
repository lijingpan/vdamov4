package com.vdamo.ordering.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("da_payment_record")
public class PaymentRecordEntity extends BaseEntity {

    @TableId
    private Long id;
    private Long orderId;
    private Long storeId;
    private String paymentNo;
    private String paymentMethod;
    private String paymentChannel;
    private Integer paidAmountInCent;
    private Integer changeAmountInCent;
    private String paymentStatus;
    private LocalDateTime paidTime;
    private String cashierName;
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

    public String getPaymentNo() {
        return paymentNo;
    }

    public void setPaymentNo(String paymentNo) {
        this.paymentNo = paymentNo;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentChannel() {
        return paymentChannel;
    }

    public void setPaymentChannel(String paymentChannel) {
        this.paymentChannel = paymentChannel;
    }

    public Integer getPaidAmountInCent() {
        return paidAmountInCent;
    }

    public void setPaidAmountInCent(Integer paidAmountInCent) {
        this.paidAmountInCent = paidAmountInCent;
    }

    public Integer getChangeAmountInCent() {
        return changeAmountInCent;
    }

    public void setChangeAmountInCent(Integer changeAmountInCent) {
        this.changeAmountInCent = changeAmountInCent;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getPaidTime() {
        return paidTime;
    }

    public void setPaidTime(LocalDateTime paidTime) {
        this.paidTime = paidTime;
    }

    public String getCashierName() {
        return cashierName;
    }

    public void setCashierName(String cashierName) {
        this.cashierName = cashierName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
