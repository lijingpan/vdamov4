package com.vdamo.ordering.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("da_order_append_log")
public class OrderAppendLogEntity extends BaseEntity {

    @TableId
    private Long id;
    private Long orderId;
    private Long storeId;
    private Integer appendRound;
    private String actionType;
    private Integer appendItemCount;
    private Integer appendAmountInCent;
    private LocalDateTime operateTime;
    private String operatorName;
    private String note;

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

    public Integer getAppendRound() {
        return appendRound;
    }

    public void setAppendRound(Integer appendRound) {
        this.appendRound = appendRound;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public Integer getAppendItemCount() {
        return appendItemCount;
    }

    public void setAppendItemCount(Integer appendItemCount) {
        this.appendItemCount = appendItemCount;
    }

    public Integer getAppendAmountInCent() {
        return appendAmountInCent;
    }

    public void setAppendAmountInCent(Integer appendAmountInCent) {
        this.appendAmountInCent = appendAmountInCent;
    }

    public LocalDateTime getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(LocalDateTime operateTime) {
        this.operateTime = operateTime;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
