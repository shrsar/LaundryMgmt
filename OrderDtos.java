package com.laundrymgmt.modern.dto;

import com.laundrymgmt.modern.model.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class OrderDtos {

    private OrderDtos() {
    }

    public record OrderRequest(Long serviceId, Integer quantity, LocalDate deliveryDate, String imageUrl,
                               String otpCode) {
    }

    public record OrderResponse(Long id, String orderCode, String customerCode, String customerName, String serviceCode,
                                String clothType, String serviceType, Integer quantity, LocalDate orderDate,
                                LocalDate deliveryDate, BigDecimal bill, String imageUrl, OrderStatus status) {
    }
}
