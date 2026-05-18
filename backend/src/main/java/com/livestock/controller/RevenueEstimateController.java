package com.livestock.controller;

import com.livestock.common.Result;
import com.livestock.dto.RevenueEstimateDTO;
import com.livestock.dto.RevenueSummaryDTO;
import com.livestock.entity.CostRecord;
import com.livestock.service.CostRecordService;
import com.livestock.service.LivestockCategoryService;
import com.livestock.service.RevenueEstimateService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/revenue")
public class RevenueEstimateController {

    private final RevenueEstimateService revenueService;
    private final CostRecordService costRecordService;
    private final LivestockCategoryService categoryService;

    public RevenueEstimateController(RevenueEstimateService revenueService,
                                     CostRecordService costRecordService,
                                     LivestockCategoryService categoryService) {
        this.revenueService = revenueService;
        this.costRecordService = costRecordService;
        this.categoryService = categoryService;
    }

    @GetMapping("/estimate")
    public Result<Map<String, Object>> estimate(@RequestParam(required = false) Long categoryId) {
        List<RevenueEstimateDTO> estimates = revenueService.estimate(categoryId);
        RevenueSummaryDTO summary = revenueService.summarize(estimates);

        // Compute today's cost and historical total, filtered by category if selected
        LocalDate today = LocalDate.now();
        List<CostRecord> allRecords = costRecordService.lambdaQuery()
                .select(CostRecord::getAmount, CostRecord::getCostDate, CostRecord::getCategoryId)
                .list();
        // If category selected, filter records to that category and its descendants
        Set<Long> filterCatIds = categoryId != null
                ? Set.copyOf(categoryService.getDescendantIds(categoryId))
                : null;
        BigDecimal todayCost = BigDecimal.ZERO;
        BigDecimal historicalTotal = BigDecimal.ZERO;
        for (CostRecord c : allRecords) {
            if (filterCatIds != null && !filterCatIds.contains(c.getCategoryId())) continue;
            BigDecimal amt = c.getAmount() != null ? c.getAmount() : BigDecimal.ZERO;
            historicalTotal = historicalTotal.add(amt);
            if (c.getCostDate() != null && c.getCostDate().equals(today)) {
                todayCost = todayCost.add(amt);
            }
        }
        summary.setTodayCost(todayCost);
        summary.setHistoricalTotalCost(historicalTotal);

        return Result.ok(Map.of("list", estimates, "summary", summary));
    }
}
