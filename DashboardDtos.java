package com.laundrymgmt.modern.dto;

import java.math.BigDecimal;
import java.util.List;

public final class DashboardDtos {

    private DashboardDtos() {
    }

    public record MetricPoint(String label, BigDecimal value) {
    }

    public record DashboardResponse(long totalCustomers, long totalOrders, BigDecimal todaysIncome,
                                    BigDecimal totalIncome, long openComplaints, List<MetricPoint> signupsTrend,
                                    List<MetricPoint> orderTrend, List<MetricPoint> revenueTrend,
                                    List<OrderDtos.OrderResponse> recentOrders,
                                    List<ComplaintDtos.ComplaintResponse> recentComplaints) {
    }
}
