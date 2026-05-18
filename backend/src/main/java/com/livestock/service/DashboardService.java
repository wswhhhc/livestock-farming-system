package com.livestock.service;

import com.livestock.dto.DashboardDTO;

import java.util.List;

public interface DashboardService {
    DashboardDTO getDashboard();
    List<DashboardDTO.CategoryDistItem> getCategoryDistribution();
    List<DashboardDTO.CostTrendItem> getCostTrend(int months);
    List<DashboardDTO.SiteUtilItem> getSiteUtilization();
    List<DashboardDTO.StageDistItem> getStageDistribution();
}
