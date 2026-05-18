package com.livestock.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.livestock.entity.*;
import com.livestock.mapper.BreedingAdviceMapper;
import com.livestock.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class BreedingAdviceServiceImpl
        extends ServiceImpl<BreedingAdviceMapper, BreedingAdvice>
        implements BreedingAdviceService {

    private final BatchService batchService;
    private final LivestockCategoryService categoryService;
    private final CostRecordService costRecordService;
    private final AiService aiService;

    public BreedingAdviceServiceImpl(BatchService batchService,
                                     LivestockCategoryService categoryService,
                                     CostRecordService costRecordService,
                                     AiService aiService) {
        this.batchService = batchService;
        this.categoryService = categoryService;
        this.costRecordService = costRecordService;
        this.aiService = aiService;
    }

    @Override
    public List<BreedingAdvice> getList(Long categoryId, Integer isRead) {
        List<Long> categoryIds = categoryService.getDescendantIds(categoryId);
        List<Batch> batches;
        if (!categoryIds.isEmpty()) {
            batches = batchService.lambdaQuery()
                    .select(Batch::getId, Batch::getBatchNo, Batch::getCategoryId)
                    .in(Batch::getCategoryId, categoryIds)
                    .list();
        } else {
            batches = batchService.lambdaQuery()
                    .select(Batch::getId, Batch::getBatchNo, Batch::getCategoryId)
                    .list();
        }
        if (batches.isEmpty()) return List.of();

        Set<Long> batchIds = batches.stream().map(Batch::getId).collect(Collectors.toSet());
        List<BreedingAdvice> list = lambdaQuery()
                .in(BreedingAdvice::getBatchId, batchIds)
                .eq(isRead != null, BreedingAdvice::getIsRead, isRead)
                .orderByDesc(BreedingAdvice::getIsRead)
                .orderByDesc(BreedingAdvice::getCreateTime)
                .list();

        Map<Long, Batch> batchMap = batches.stream().collect(Collectors.toMap(Batch::getId, b -> b));
        List<LivestockCategory> categories = categoryService.list();

        for (BreedingAdvice a : list) {
            Batch b = batchMap.get(a.getBatchId());
            if (b != null) {
                a.setBatchNo(b.getBatchNo());
                categories.stream().filter(c -> c.getId().equals(b.getCategoryId())).findFirst()
                        .ifPresent(c -> a.setCategoryName(c.getCategoryName()));
            }
        }
        return list;
    }

    @Override
    @Transactional
    public void generate() {
        List<Batch> batches = batchService.lambdaQuery()
                .eq(Batch::getStatus, 1)
                .list();
        if (batches.isEmpty()) return;

        List<LivestockCategory> categories = categoryService.list();
        Map<Long, LivestockCategory> catMap = categories.stream()
                .collect(Collectors.toMap(LivestockCategory::getId, c -> c));

        List<CostRecord> allCosts = costRecordService.list();
        boolean aiEnabled = aiService.isEnabled();

        List<BreedingAdvice> newAdvice = new ArrayList<>();
        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();

        // === 并行 AI 调用（最多 3 个并发） ===
        Map<Long, String> aiResults = new HashMap<>();
        if (aiEnabled) {
            List<Batch> aiBatches = batches.stream()
                    .filter(b -> catMap.containsKey(b.getCategoryId()))
                    .filter(b -> lambdaQuery()
                            .eq(BreedingAdvice::getBatchId, b.getId())
                            .eq(BreedingAdvice::getTriggerType, 5)
                            .eq(BreedingAdvice::getIsRead, 0)
                            .count() == 0)
                    .collect(Collectors.toList());

            if (!aiBatches.isEmpty()) {
                ExecutorService executor = Executors.newFixedThreadPool(3);
                List<CompletableFuture<Map.Entry<Long, String>>> futures = new ArrayList<>();

                for (Batch batch : aiBatches) {
                    LivestockCategory cat = catMap.get(batch.getCategoryId());
                    Map<String, Object> ctx = buildAiContext(batch, cat, allCosts, today, currentMonth);
                    futures.add(CompletableFuture.supplyAsync(() -> {
                        String content = aiService.generateAdvice(ctx);
                        if (content != null && !content.isBlank()) {
                            return new AbstractMap.SimpleEntry<>(batch.getId(), content);
                        }
                        return null;
                    }, executor));
                }

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                executor.shutdown();

                for (CompletableFuture<Map.Entry<Long, String>> f : futures) {
                    Map.Entry<Long, String> entry = f.join();
                    if (entry != null) {
                        aiResults.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }

        for (Batch batch : batches) {
            LivestockCategory cat = catMap.get(batch.getCategoryId());
            if (cat == null) continue;

            // === AI 结果写入 ===
            String aiContent = aiResults.get(batch.getId());
            if (aiContent != null) {
                BreedingAdvice a = new BreedingAdvice();
                a.setBatchId(batch.getId());
                a.setGrowthStage(batch.getCurrentStage());
                a.setAdviceContent(aiContent);
                a.setIsRead(0);
                a.setTriggerType(5);
                newAdvice.add(a);
            }

            // stage 4 = ready for slaughter
            if (batch.getCurrentStage() != null && batch.getCurrentStage() == 4) {
                addSystemAdvice(newAdvice, batch, 1,
                        "批次 " + batch.getBatchNo() + " 已达出栏阶段，建议尽快安排出栏。");
            }

            // past expected slaughter date
            if (batch.getExpectedSlaughterDate() != null
                    && today.isAfter(batch.getExpectedSlaughterDate())) {
                long daysPast = ChronoUnit.DAYS.between(batch.getExpectedSlaughterDate(), today);
                addSystemAdvice(newAdvice, batch, 1,
                        "批次 " + batch.getBatchNo() + " 已超过预计出栏日 " + daysPast + " 天，请注意调整饲养计划。");
            }

            // low stock warning
            if (batch.getInitialQuantity() != null && batch.getCurrentQuantity() != null
                    && batch.getCurrentQuantity() < batch.getInitialQuantity() * 0.3) {
                addSystemAdvice(newAdvice, batch, 2,
                        "批次 " + batch.getBatchNo() + " 存栏仅剩 " + batch.getCurrentQuantity()
                                + " 头（初始 " + batch.getInitialQuantity() + "），请关注存栏情况。");
            }

            // cost check
            BigDecimal batchCost = allCosts.stream()
                    .filter(c -> batch.getId().equals(c.getBatchId()))
                    .map(CostRecord::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal categoryCost = allCosts.stream()
                    .filter(c -> c.getBatchId() == null && batch.getCategoryId().equals(c.getCategoryId()))
                    .map(CostRecord::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalCost = batchCost.add(categoryCost);

            if (cat.getBasePrice() != null && batch.getCurrentQuantity() != null
                    && batch.getCurrentQuantity() > 0) {
                BigDecimal estimatedValue = cat.getBasePrice()
                        .multiply(cat.getSlaughterWeight() != null ? cat.getSlaughterWeight() : BigDecimal.ONE)
                        .multiply(cat.getEstimatedSlaughterRate() != null ? cat.getEstimatedSlaughterRate() : BigDecimal.valueOf(50))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(batch.getCurrentQuantity()));

                if (estimatedValue.compareTo(BigDecimal.ZERO) > 0
                        && totalCost.compareTo(estimatedValue.multiply(BigDecimal.valueOf(0.5))) > 0) {
                    addSystemAdvice(newAdvice, batch, 3,
                            "批次 " + batch.getBatchNo() + " 累计成本 ¥" + totalCost
                                    + "，已超过预估产值的 50%，请关注成本控制。");
                }
            }
        }

        if (!newAdvice.isEmpty()) {
            saveBatch(newAdvice);
        }
    }

    private Map<String, Object> buildAiContext(Batch batch, LivestockCategory cat,
                                                List<CostRecord> allCosts, LocalDate today, int currentMonth) {
        Map<String, Object> ctx = new LinkedHashMap<>();
        ctx.put("批次编号", batch.getBatchNo());
        ctx.put("种类名称", cat.getCategoryName());
        ctx.put("当前生长阶段", stageLabel(batch.getCurrentStage()));

        if (batch.getCurrentQuantity() != null) {
            ctx.put("当前存栏量", batch.getCurrentQuantity() + " 头");
            if (batch.getInitialQuantity() != null) {
                ctx.put("初始存栏量", batch.getInitialQuantity() + " 头");
                double survivalRate = batch.getCurrentQuantity() * 100.0 / batch.getInitialQuantity();
                ctx.put("存活率", String.format("%.1f%%", survivalRate));
            }
        }

        if (batch.getEntryDate() != null) {
            ctx.put("入场日期", batch.getEntryDate().toString());
            long days = ChronoUnit.DAYS.between(batch.getEntryDate(), today);
            ctx.put("已饲养天数", days + " 天");
        }

        if (batch.getExpectedSlaughterDate() != null) {
            ctx.put("预计出栏日", batch.getExpectedSlaughterDate().toString());
            long remaining = ChronoUnit.DAYS.between(today, batch.getExpectedSlaughterDate());
            ctx.put("剩余饲养天数", Math.max(0, remaining) + " 天");
        }

        if (cat.getGrowthCycle() != null) {
            ctx.put("生长周期", cat.getGrowthCycle() + " 天");
        }
        if (cat.getSlaughterWeight() != null) {
            ctx.put("出栏标准体重", cat.getSlaughterWeight() + " kg");
        }
        if (cat.getFeedType() != null) {
            ctx.put("推荐饲料类型", cat.getFeedType());
        }
        if (cat.getCommonDiseases() != null) {
            ctx.put("常见病害", cat.getCommonDiseases());
        }

        // 成本统计
        BigDecimal feedCost = allCosts.stream()
                .filter(c -> batch.getId().equals(c.getBatchId()) && c.getCostType() == 1)
                .map(CostRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal seedlingCost = allCosts.stream()
                .filter(c -> batch.getId().equals(c.getBatchId()) && c.getCostType() == 2)
                .map(CostRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal medicalCost = allCosts.stream()
                .filter(c -> batch.getId().equals(c.getBatchId()) && c.getCostType() == 3)
                .map(CostRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal laborCost = allCosts.stream()
                .filter(c -> batch.getId().equals(c.getBatchId()) && c.getCostType() == 4)
                .map(CostRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCost = feedCost.add(seedlingCost).add(medicalCost).add(laborCost);

        if (feedCost.compareTo(BigDecimal.ZERO) > 0) ctx.put("累计饲料成本", "¥" + feedCost);
        if (seedlingCost.compareTo(BigDecimal.ZERO) > 0) ctx.put("累计苗种成本", "¥" + seedlingCost);
        if (medicalCost.compareTo(BigDecimal.ZERO) > 0) ctx.put("累计防疫人工成本", "¥" + medicalCost);
        if (laborCost.compareTo(BigDecimal.ZERO) > 0) ctx.put("累计其他成本", "¥" + laborCost);
        if (totalCost.compareTo(BigDecimal.ZERO) > 0) ctx.put("总成本", "¥" + totalCost);

        // 预估收益（简化计算）
        if (cat.getBasePrice() != null && cat.getSlaughterWeight() != null
                && cat.getEstimatedSlaughterRate() != null && batch.getCurrentQuantity() != null) {
            BigDecimal estimatedRevenue = cat.getBasePrice()
                    .multiply(cat.getSlaughterWeight())
                    .multiply(cat.getEstimatedSlaughterRate())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(batch.getCurrentQuantity()));
            ctx.put("预估收入", "¥" + estimatedRevenue);

            BigDecimal profit = estimatedRevenue.subtract(totalCost);
            ctx.put("预估利润", "¥" + profit);
        }

        String[] months = {"", "一月", "二月", "三月", "四月", "五月", "六月",
                "七月", "八月", "九月", "十月", "十一月", "十二月"};
        ctx.put("当前月份", months[currentMonth]);

        return ctx;
    }

    private void addSystemAdvice(List<BreedingAdvice> list, Batch batch, int triggerType, String content) {
        boolean exists = lambdaQuery()
                .eq(BreedingAdvice::getBatchId, batch.getId())
                .eq(BreedingAdvice::getAdviceContent, content)
                .count() > 0;
        if (!exists) {
            BreedingAdvice a = new BreedingAdvice();
            a.setBatchId(batch.getId());
            a.setGrowthStage(batch.getCurrentStage());
            a.setAdviceContent(content);
            a.setIsRead(0);
            a.setTriggerType(triggerType);
            list.add(a);
        }
    }

    @Override
    public void markRead(Long id) {
        lambdaUpdate()
                .eq(BreedingAdvice::getId, id)
                .set(BreedingAdvice::getIsRead, 1)
                .update();
    }

    static String stageLabel(Integer stage) {
        if (stage == null) return "未知";
        String[] labels = {"", "苗种阶段", "青年阶段", "成年阶段", "出栏前阶段"};
        return stage >= 0 && stage < labels.length ? labels[stage] : "未知";
    }
}
