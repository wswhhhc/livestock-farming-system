package com.livestock.service.impl;

import com.livestock.dto.DashboardDTO;
import com.livestock.dto.RevenueEstimateDTO;
import com.livestock.dto.RevenueSummaryDTO;
import com.livestock.entity.*;
import com.livestock.service.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final BatchService batchService;
    private final LivestockCategoryService categoryService;
    private final BreedingSiteService siteService;
    private final BreedingAdviceService adviceService;
    private final RevenueEstimateService revenueService;
    private final CostRecordService costRecordService;

    public DashboardServiceImpl(BatchService batchService,
                                LivestockCategoryService categoryService,
                                BreedingSiteService siteService,
                                BreedingAdviceService adviceService,
                                RevenueEstimateService revenueService,
                                CostRecordService costRecordService) {
        this.batchService = batchService;
        this.categoryService = categoryService;
        this.siteService = siteService;
        this.adviceService = adviceService;
        this.revenueService = revenueService;
        this.costRecordService = costRecordService;
    }

    @Override
    public DashboardDTO getDashboard() {
        DashboardDTO dto = new DashboardDTO();

        // summary
        DashboardDTO.Summary summary = new DashboardDTO.Summary();
        List<Batch> activeBatches = batchService.lambdaQuery()
                .eq(Batch::getStatus, 1)
                .list();
        summary.setActiveBatchCount(activeBatches.size());
        summary.setTotalQuantity(activeBatches.stream()
                .mapToInt(b -> b.getCurrentQuantity() != null ? b.getCurrentQuantity() : 0)
                .sum());
        summary.setCategoryCount((int) categoryService.count());
        summary.setSiteCount((int) siteService.count());
        dto.setSummary(summary);

        // pending
        DashboardDTO.Pending pending = new DashboardDTO.Pending();
        pending.setUnreadAdvice(adviceService.lambdaQuery().eq(BreedingAdvice::getIsRead, 0).count());
        pending.setSlaughterReady(activeBatches.stream()
                .filter(b -> b.getCurrentStage() != null && b.getCurrentStage() == 4)
                .count());
        LocalDate today = LocalDate.now();
        pending.setOverdueBatches(activeBatches.stream()
                .filter(b -> b.getExpectedSlaughterDate() != null && today.isAfter(b.getExpectedSlaughterDate()))
                .count());
        dto.setPending(pending);

        // revenue
        List<RevenueEstimateDTO> estimates = revenueService.estimate(null);
        RevenueSummaryDTO revSummary = revenueService.summarize(estimates);
        DashboardDTO.Revenue revenue = new DashboardDTO.Revenue();
        revenue.setEstimatedRevenue(revSummary.getTotalEstimatedRevenue() != null ? revSummary.getTotalEstimatedRevenue() : BigDecimal.ZERO);
        revenue.setEstimatedProfit(revSummary.getTotalEstimatedProfit() != null ? revSummary.getTotalEstimatedProfit() : BigDecimal.ZERO);
        revenue.setTotalCost(revSummary.getTotalCost() != null ? revSummary.getTotalCost() : BigDecimal.ZERO);
        revenue.setProfitMargin(revSummary.getOverallProfitMargin() != null ? revSummary.getOverallProfitMargin() : "-");
        dto.setRevenue(revenue);

        return dto;
    }

    @Override
    public List<DashboardDTO.CategoryDistItem> getCategoryDistribution() {
        List<Batch> active = batchService.lambdaQuery().eq(Batch::getStatus, 1).list();
        Map<Long, List<Batch>> grouped = active.stream()
                .filter(b -> b.getCategoryId() != null)
                .collect(Collectors.groupingBy(Batch::getCategoryId));

        List<LivestockCategory> allCats = categoryService.lambdaQuery().list();
        Map<Long, String> catNames = allCats.stream()
                .collect(Collectors.toMap(LivestockCategory::getId, LivestockCategory::getCategoryName));

        List<DashboardDTO.CategoryDistItem> result = new ArrayList<>();
        for (Map.Entry<Long, List<Batch>> entry : grouped.entrySet()) {
            DashboardDTO.CategoryDistItem item = new DashboardDTO.CategoryDistItem();
            item.setName(catNames.getOrDefault(entry.getKey(), "未知"));
            item.setBatchCount(entry.getValue().size());
            item.setQuantity(entry.getValue().stream()
                    .mapToInt(b -> b.getCurrentQuantity() != null ? b.getCurrentQuantity() : 0)
                    .sum());
            result.add(item);
        }
        result.sort((a, b) -> Integer.compare(b.getQuantity(), a.getQuantity()));
        return result;
    }

    @Override
    public List<DashboardDTO.CostTrendItem> getCostTrend(int months) {
        LocalDate start = LocalDate.now().minusMonths(months);
        List<CostRecord> records = costRecordService.lambdaQuery()
                .ge(CostRecord::getCostDate, start)
                .list();

        Map<String, List<CostRecord>> byMonth = records.stream()
                .filter(r -> r.getCostDate() != null)
                .collect(Collectors.groupingBy(r -> r.getCostDate().toString().substring(0, 7)));

        List<DashboardDTO.CostTrendItem> result = new ArrayList<>();
        for (int i = months - 1; i >= 0; i--) {
            LocalDate m = LocalDate.now().minusMonths(i);
            String key = m.toString().substring(0, 7);
            DashboardDTO.CostTrendItem item = new DashboardDTO.CostTrendItem();
            item.setMonth(key);
            List<CostRecord> monthRecords = byMonth.get(key);
            item.setTotalAmount(monthRecords != null
                    ? monthRecords.stream().map(r -> r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2));
            result.add(item);
        }
        return result;
    }

    @Override
    public List<DashboardDTO.SiteUtilItem> getSiteUtilization() {
        List<BreedingSite> sites = siteService.lambdaQuery().list();

        List<Batch> active = batchService.lambdaQuery().eq(Batch::getStatus, 1).list();
        Map<Long, Integer> stockBySite = active.stream()
                .filter(b -> b.getSiteId() != null)
                .collect(Collectors.groupingBy(
                        Batch::getSiteId,
                        Collectors.summingInt(b -> b.getCurrentQuantity() != null ? b.getCurrentQuantity() : 0)
                ));

        List<DashboardDTO.SiteUtilItem> result = new ArrayList<>();
        for (BreedingSite site : sites) {
            DashboardDTO.SiteUtilItem item = new DashboardDTO.SiteUtilItem();
            item.setSiteName(site.getSiteName());
            int cap = site.getCapacity() != null ? site.getCapacity() : 0;
            int stock = stockBySite.getOrDefault(site.getId(), 0);
            item.setCapacity(cap);
            item.setCurrentStock(stock);
            item.setUsageRate(cap > 0 ? BigDecimal.valueOf(stock * 100.0 / cap)
                    .setScale(1, RoundingMode.HALF_UP).doubleValue() : 0);
            result.add(item);
        }
        return result;
    }

    @Override
    public List<DashboardDTO.StageDistItem> getStageDistribution() {
        List<Batch> active = batchService.lambdaQuery().eq(Batch::getStatus, 1).list();
        String[] stages = {"", "苗种", "青年", "成年", "出栏前"};

        Map<Integer, List<Batch>> grouped = active.stream()
                .filter(b -> b.getCurrentStage() != null)
                .collect(Collectors.groupingBy(Batch::getCurrentStage));

        List<DashboardDTO.StageDistItem> result = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            DashboardDTO.StageDistItem item = new DashboardDTO.StageDistItem();
            item.setStage(stages[i]);
            List<Batch> list = grouped.get(i);
            item.setCount(list != null ? list.stream()
                    .mapToInt(b -> b.getCurrentQuantity() != null ? b.getCurrentQuantity() : 0)
                    .sum() : 0);
            result.add(item);
        }
        return result;
    }
}
