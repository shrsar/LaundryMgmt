package com.laundrymgmt.modern.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "service_catalog")
public class LaundryService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String serviceCode;

    @Column(nullable = false, length = 80)
    private String clothType;

    @Column(nullable = false, length = 80)
    private String serviceType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerItem;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal premiumPerDay;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getClothType() {
        return clothType;
    }

    public void setClothType(String clothType) {
        this.clothType = clothType;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public BigDecimal getPricePerItem() {
        return pricePerItem;
    }

    public void setPricePerItem(BigDecimal pricePerItem) {
        this.pricePerItem = pricePerItem;
    }

    public BigDecimal getPremiumPerDay() {
        return premiumPerDay;
    }

    public void setPremiumPerDay(BigDecimal premiumPerDay) {
        this.premiumPerDay = premiumPerDay;
    }
}
