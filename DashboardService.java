package com.laundrymgmt.modern.service;

import com.laundrymgmt.modern.dto.ComplaintDtos;
import com.laundrymgmt.modern.dto.DashboardDtos;
import com.laundrymgmt.modern.dto.OrderDtos;
import com.laundrymgmt.modern.model.ComplaintStatus;
import com.laundrymgmt.modern.model.Role;
import com.laundrymgmt.modern.repository.ComplaintRepository;
import com.laundrymgmt.modern.repository.LaundryOrderRepository;
import com.laundrymgmt.modern.repository.UserAccountRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final SessionService sessionService;
    private final UserAccountRepository userAccountRepository;
    private final LaundryOrderRepository laundryOrderRepository;
    private final ComplaintRepository complaintRepository;
    private final OrderService orderService;
    private final ComplaintService complaintService;

    public DashboardService(SessionService sessionService,
                            UserAccountRepository userAccountRepository,
                            LaundryOrderRepository laundryOrderRepository,
                            ComplaintRepository complaintRepository,
                            OrderService orderService,
                            ComplaintService complaintService) {
        this.sessionService = sessionService;
        this.userAccountRepository = userAccountRepository;
        this.laundryOrderRepository = laundryOrderRepository;
        this.complaintRepository = complaintRepository;
        this.orderService = orderService;
        this.complaintService = complaintService;
    }

    public DashboardDtos.DashboardResponse getDashboard(String authorizationHeader) {
        sessionService.requireRole(authorizationHeader, Role.ADMIN);

        long totalCustomers = userAccountRepository.countByRole(Role.CUSTOMER);
        long totalOrders = laundryOrderRepository.count();
        BigDecimal todaysIncome = laundryOrderRepository.sumRevenueByDate(LocalDate.now());
        BigDecimal totalIncome = laundryOrderRepository.sumAllRevenue();
        long openComplaints = complaintRepository.countByStatus(ComplaintStatus.OPEN);

        List<DashboardDtos.MetricPoint> signupsTrend = toMetricPoints(userAccountRepository.countSignupsByDate(Role.CUSTOMER));
        List<DashboardDtos.MetricPoint> orderTrend = toMetricPoints(laundryOrderRepository.countOrdersByDate());
        List<DashboardDtos.MetricPoint> revenueTrend = toMetricPoints(laundryOrderRepository.sumRevenueGroupedByDate());
        List<OrderDtos.OrderResponse> recentOrders = laundryOrderRepository.findTop5ByOrderByOrderDateDescIdDesc().stream()
            .map(orderService::toResponse)
            .toList();
        List<ComplaintDtos.ComplaintResponse> recentComplaints = complaintRepository.findTop5ByOrderByCreatedAtDescIdDesc().stream()
            .map(complaintService::toResponse)
            .toList();

        return new DashboardDtos.DashboardResponse(
            totalCustomers,
            totalOrders,
            todaysIncome,
            totalIncome,
            openComplaints,
            signupsTrend,
            orderTrend,
            revenueTrend,
            recentOrders,
            recentComplaints
        );
    }

    private List<DashboardDtos.MetricPoint> toMetricPoints(List<Object[]> rows) {
        return rows.stream()
            .map(row -> new DashboardDtos.MetricPoint(row[0].toString(), toBigDecimal(row[1])))
            .toList();
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Long longValue) {
            return BigDecimal.valueOf(longValue);
        }
        if (value instanceof Integer integerValue) {
            return BigDecimal.valueOf(integerValue);
        }
        return new BigDecimal(value.toString());
    }
}
