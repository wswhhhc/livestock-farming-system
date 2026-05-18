package com.livestock.service;

import com.livestock.dto.RevenueEstimateDTO;
import com.livestock.dto.RevenueSummaryDTO;

import java.util.List;

public interface RevenueEstimateService {
    List<RevenueEstimateDTO> estimate(Long categoryId);
    RevenueSummaryDTO summarize(List<RevenueEstimateDTO> estimates);
}
