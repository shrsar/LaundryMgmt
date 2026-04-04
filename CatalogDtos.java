package com.laundrymgmt.modern.dto;

import java.math.BigDecimal;

public final class CatalogDtos {

    private CatalogDtos() {
    }

    public record ServiceRequest(String serviceCode, String clothType, String serviceType, BigDecimal pricePerItem,
                                 BigDecimal premiumPerDay) {
    }

    public record ServiceResponse(Long id, String serviceCode, String clothType, String serviceType,
                                  BigDecimal pricePerItem, BigDecimal premiumPerDay) {
    }
}
