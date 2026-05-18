package com.livestock.controller;

import com.livestock.common.Result;
import com.livestock.dto.DashboardDTO;
import com.livestock.service.DashboardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public Result<DashboardDTO> getDashboard() {
        return Result.ok(dashboardService.getDashboard());
    }

    @GetMapping("/category-dist")
    public Result<List<DashboardDTO.CategoryDistItem>> getCategoryDistribution() {
        return Result.ok(dashboardService.getCategoryDistribution());
    }

    @GetMapping("/cost-trend")
    public Result<List<DashboardDTO.CostTrendItem>> getCostTrend(
            @RequestParam(defaultValue = "6") int months) {
        return Result.ok(dashboardService.getCostTrend(months));
    }

    @GetMapping("/site-util")
    public Result<List<DashboardDTO.SiteUtilItem>> getSiteUtilization() {
        return Result.ok(dashboardService.getSiteUtilization());
    }

    @GetMapping("/stage-dist")
    public Result<List<DashboardDTO.StageDistItem>> getStageDistribution() {
        return Result.ok(dashboardService.getStageDistribution());
    }
}
