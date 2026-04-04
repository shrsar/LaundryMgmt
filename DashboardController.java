package com.laundrymgmt.modern.controller;

import com.laundrymgmt.modern.dto.DashboardDtos;
import com.laundrymgmt.modern.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardDtos.DashboardResponse getDashboard(
        @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return dashboardService.getDashboard(authorizationHeader);
    }
}
