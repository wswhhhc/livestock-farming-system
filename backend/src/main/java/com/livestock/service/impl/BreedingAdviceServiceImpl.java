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
import java.util.stream.Collectors;

@Service
public class BreedingAdviceServiceImpl
        extends ServiceImpl<BreedingAdviceMapper, BreedingAdvice>
        implements BreedingAdviceService {

    private final BatchService batchService;
    private final LivestockCategoryService categoryService;
    private final BreedingAdviceTemplateService templateService;
    private final CostRecordService costRecordService;

    public BreedingAdviceServiceImpl(BatchService batchService,
                                     LivestockCategoryService categoryService,
                                     BreedingAdviceTemplateService templateService,
                                     CostRecordService costRecordService) {
        this.batchService = batchService;
        this.categoryService = categoryService;
        this.templateService = templateService;
        this.costRecordService = costRecordService;
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

        List<BreedingAdviceTemplate> templates = templateService.list();
        Map<String, List<BreedingAdviceTemplate>> templateMap = templates.stream()
                .collect(Collectors.groupingBy(t -> t.getCategoryId() + "_" + t.getGrowthStage()));

        List<CostRecord> allCosts = costRecordService.list();

        List<BreedingAdvice> newAdvice = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Batch batch : batches) {
            LivestockCategory cat = catMap.get(batch.getCategoryId());
            if (cat == null) continue;

            String key = batch.getCategoryId() + "_" + batch.getCurrentStage();
            List<BreedingAdviceTemplate> matchedTemplates = templateMap.getOrDefault(key, List.of());

            for (BreedingAdviceTemplate tpl : matchedTemplates) {
                boolean exists = lambdaQuery()
                        .eq(BreedingAdvice::getBatchId, batch.getId())
                        .eq(BreedingAdvice::getTemplateId, tpl.getId())
                        .count() > 0;
                if (!exists) {
                    BreedingAdvice a = new BreedingAdvice();
                    a.setBatchId(batch.getId());
                    a.setTemplateId(tpl.getId());
                    a.setGrowthStage(batch.getCurrentStage());
                    a.setAdviceContent(tpl.getAdviceContent());
                    a.setIsRead(0);
                    a.setTriggerType(parseTriggerType(tpl.getAdviceType()));
                    newAdvice.add(a);
                }
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

    private int parseTriggerType(String adviceType) {
        if (adviceType == null) return 0;
        switch (adviceType) {
            case "slaughter": return 1;
            case "stock": return 2;
            case "cost": return 3;
            case "price": return 4;
            default: return 0;
        }
    }
}
