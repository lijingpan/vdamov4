package com.vdamo.ordering.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("da_store")
public class StoreEntity extends BaseEntity {

    @TableId
    private Long id;
    private String name;
    private String countryCode;
    private String status;
    private String businessModes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBusinessModes() {
        return businessModes;
    }

    public void setBusinessModes(String businessModes) {
        this.businessModes = businessModes;
    }
}
