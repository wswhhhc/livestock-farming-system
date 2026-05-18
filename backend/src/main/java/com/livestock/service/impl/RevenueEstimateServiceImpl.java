package com.livestock.service.impl;

import com.livestock.dto.RevenueEstimateDTO;
import com.livestock.dto.RevenueSummaryDTO;
import com.livestock.entity.Batch;
import com.livestock.entity.LivestockCategory;
import com.livestock.entity.PriceRule;
import com.livestock.service.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RevenueEstimateServiceImpl implements RevenueEstimateService {

    private final BatchService batchService;
    private final LivestockCategoryService categoryService;
    private final PriceRuleService priceRuleService;

    public RevenueEstimateServiceImpl(BatchService batchService,
                                      LivestockCategoryService categoryService,
                                      PriceRuleService priceRuleService) {
        this.batchService = batchService;
        this.categoryService = categoryService;
        this.priceRuleService = priceRuleService;
    }

    /**
     * 预估模型：
     *   - 每日每头饲料费 = 饲料日均消耗量 × 饲料单价（来自种类基础数据）
     *   - 每日每头管理费 = 家畜种类中设定的每日管理费
     *   - 总成本 =（每日饲料费 + 每日管理费）× 生长周期（出栏天数）× 当前数量
     *   - 收入 = 当前数量 × 出栏率 × 出栏体重 × 调整后价格
     *   - 利润 = 收入 - 总成本
     */
    @Override
    public List<RevenueEstimateDTO> estimate(Long categoryId) {
        List<Long> categoryIds = categoryService.getDescendantIds(categoryId);
        List<Batch> batches = batchService.lambdaQuery()
                .in(!categoryIds.isEmpty(), Batch::getCategoryId, categoryIds)
                .eq(Batch::getStatus, 1)
                .orderByDesc(Batch::getId)
                .list();

        List<LivestockCategory> categories = categoryService.list();
        List<PriceRule> allRules = priceRuleService.lambdaQuery()
                .eq(PriceRule::getEnabled, 1)
                .orderByAsc(PriceRule::getPriority)
                .list();

        Map<Long, LivestockCategory> catMap = categories.stream()
                .collect(Collectors.toMap(LivestockCategory::getId, c -> c));

        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();

        List<RevenueEstimateDTO> result = new ArrayList<>();
        for (Batch batch : batches) {
            LivestockCategory cat = catMap.get(batch.getCategoryId());
            if (cat == null) continue;

            BigDecimal basePrice = cat.getBasePrice();
            BigDecimal weight = cat.getSlaughterWeight();
            BigDecimal rate = cat.getEstimatedSlaughterRate();
            int qty = batch.getCurrentQuantity() != null ? batch.getCurrentQuantity() : 0;

            // Skip categories without price data
            if (basePrice == null || weight == null || rate == null) continue;

            // ---- Growth cycle (出栏天数) ----
            int growthCycle = cat.getGrowthCycle() != null ? cat.getGrowthCycle() : 0;

            // ---- Daily feed cost per animal ----
            BigDecimal dailyFeedPerAnimal = BigDecimal.ZERO;
            if (cat.getFeedPrice() != null && cat.getFeedPrice().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal avgConsumption = BigDecimal.ZERO;
                int cnt = 0;
                if (cat.getFeedConsumptionSeedling() != null) {
                    avgConsumption = avgConsumption.add(cat.getFeedConsumptionSeedling()); cnt++;
                }
                if (cat.getFeedConsumptionYoung() != null) {
                    avgConsumption = avgConsumption.add(cat.getFeedConsumptionYoung()); cnt++;
                }
                if (cat.getFeedConsumptionAdult() != null) {
                    avgConsumption = avgConsumption.add(cat.getFeedConsumptionAdult()); cnt++;
                }
                if (cnt > 0) {
                    avgConsumption = avgConsumption.divide(BigDecimal.valueOf(cnt), 4, RoundingMode.HALF_UP);
                    dailyFeedPerAnimal = avgConsumption.multiply(cat.getFeedPrice()).setScale(4, RoundingMode.HALF_UP);
                }
            }

            // ---- Total lifecycle cost: (dailyFeed + dailyMgmt) x growthCycle x qty ----
            BigDecimal totalFeedCost = dailyFeedPerAnimal
                    .multiply(BigDecimal.valueOf(growthCycle))
                    .multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);

            BigDecimal dailyMgmt = cat.getDailyMgmtFee() != null ? cat.getDailyMgmtFee() : BigDecimal.ZERO;
            BigDecimal totalMgmtCost = dailyMgmt
                    .multiply(BigDecimal.valueOf(growthCycle))
                    .multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);

            BigDecimal totalCost = totalFeedCost.add(totalMgmtCost).setScale(2, RoundingMode.HALF_UP);

            // ---- Applicable price rules ----
            List<PriceRule> applicableRules = allRules.stream()
                    .filter(r -> r.getCategoryId().equals(batch.getCategoryId()))
                    .filter(r -> evaluateCondition(r.getApplyCondition(), currentMonth))
                    .collect(Collectors.toList());

            BigDecimal totalRatio = applicableRules.stream()
                    .map(r -> r.getDirection() == 1 ? r.getRatio() : r.getRatio().negate())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal adjustedPrice = basePrice.multiply(
                            BigDecimal.ONE.add(totalRatio.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)))
                    .setScale(2, RoundingMode.HALF_UP);

            // ---- Revenue ----
            BigDecimal estimatedWeight = weight.multiply(BigDecimal.valueOf(qty));
            BigDecimal meatWeight = estimatedWeight.multiply(rate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal revenue = meatWeight.multiply(adjustedPrice).setScale(2, RoundingMode.HALF_UP);
            BigDecimal profit = revenue.subtract(totalCost).setScale(2, RoundingMode.HALF_UP);

            String margin = revenue.compareTo(BigDecimal.ZERO) > 0
                    ? profit.divide(revenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP) + "%"
                    : "-";

            String adjustDesc = applicableRules.isEmpty() ? "基准价" :
                    applicableRules.stream()
                            .map(r -> r.getRuleName() + (r.getDirection() == 1 ? "(+" : "(-") + r.getRatio() + "%)")
                            .collect(Collectors.joining(", "));

            RevenueEstimateDTO dto = new RevenueEstimateDTO();
            dto.setBatchId(batch.getId());
            dto.setBatchNo(batch.getBatchNo());
            dto.setCategoryId(batch.getCategoryId());
            dto.setCategoryName(cat.getCategoryName());
            dto.setCurrentStage(batch.getCurrentStage());
            dto.setCurrentQuantity(qty);
            dto.setEntryDate(batch.getEntryDate() != null ? batch.getEntryDate().toString() : null);
            dto.setExpectedSlaughterDate(batch.getExpectedSlaughterDate() != null ? batch.getExpectedSlaughterDate().toString() : null);
            dto.setBasePrice(basePrice);
            dto.setSlaughterWeight(weight);
            dto.setEstimatedSlaughterRate(rate);
            dto.setAdjustedPrice(adjustedPrice);
            dto.setPriceAdjustDesc(adjustDesc);
            dto.setEstimatedWeight(meatWeight);
            dto.setEstimatedRevenue(revenue);
            dto.setProjectedFeedCost(totalFeedCost);
            dto.setProjectedLaborWaterEquip(totalMgmtCost);
            dto.setMedicalOtherCost(BigDecimal.ZERO);
            dto.setTotalCost(totalCost);
            dto.setEstimatedProfit(profit);
            dto.setProfitMargin(margin);
            dto.setApplicableRuleCount(applicableRules.size());
            result.add(dto);
        }
        return result;
    }

    @Override
    public RevenueSummaryDTO summarize(List<RevenueEstimateDTO> estimates) {
        RevenueSummaryDTO s = new RevenueSummaryDTO();
        s.setBatchCount(estimates.size());
        s.setTotalQuantity(estimates.stream().mapToInt(RevenueEstimateDTO::getCurrentQuantity).sum());
        s.setTotalEstimatedWeight(estimates.stream()
                .map(RevenueEstimateDTO::getEstimatedWeight).reduce(BigDecimal.ZERO, BigDecimal::add));
        s.setTotalEstimatedRevenue(estimates.stream()
                .map(RevenueEstimateDTO::getEstimatedRevenue).reduce(BigDecimal.ZERO, BigDecimal::add));
        s.setTotalCost(estimates.stream()
                .map(RevenueEstimateDTO::getTotalCost).reduce(BigDecimal.ZERO, BigDecimal::add));
        s.setTotalEstimatedProfit(estimates.stream()
                .map(RevenueEstimateDTO::getEstimatedProfit).reduce(BigDecimal.ZERO, BigDecimal::add));

        BigDecimal revenue = s.getTotalEstimatedRevenue();
        BigDecimal profit = s.getTotalEstimatedProfit();
        s.setOverallProfitMargin(revenue.compareTo(BigDecimal.ZERO) > 0
                ? profit.divide(revenue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP) + "%"
                : "-");
        return s;
    }

    private boolean evaluateCondition(String condition, int currentMonth) {
        if (condition == null || condition.isBlank()) return true;
        String cond = condition.replaceAll("\\s+", "").toLowerCase();
        if (cond.startsWith("monthin(") && cond.endsWith(")")) {
            String inner = cond.substring(8, cond.length() - 1);
            return Arrays.stream(inner.split(","))
                    .map(String::trim)
                    .anyMatch(m -> m.equals(String.valueOf(currentMonth)));
        }
        if (cond.startsWith("month=")) {
            String val = cond.substring(6).trim();
            return val.equals(String.valueOf(currentMonth));
        }
        return false;
    }
}
