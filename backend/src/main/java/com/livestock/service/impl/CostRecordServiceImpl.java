package com.livestock.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.livestock.entity.Batch;
import com.livestock.entity.CostRecord;
import com.livestock.entity.LivestockCategory;
import com.livestock.mapper.CostRecordMapper;
import com.livestock.service.BatchService;
import com.livestock.service.CostRecordService;
import com.livestock.service.LivestockCategoryService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CostRecordServiceImpl extends ServiceImpl<CostRecordMapper, CostRecord>
        implements CostRecordService {

    private final LivestockCategoryService categoryService;
    private final BatchService batchService;

    public CostRecordServiceImpl(LivestockCategoryService categoryService, BatchService batchService) {
        this.categoryService = categoryService;
        this.batchService = batchService;
    }

    private String generateBatchNo() {
        String prefix = "CJ" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        CostRecord last = lambdaQuery()
                .likeRight(CostRecord::getBatchNo, prefix)
                .orderByDesc(CostRecord::getBatchNo)
                .last("LIMIT 1")
                .one();
        int seq = 1;
        if (last != null && last.getBatchNo() != null && last.getBatchNo().length() > prefix.length()) {
            seq = Integer.parseInt(last.getBatchNo().substring(prefix.length())) + 1;
        }
        return prefix + String.format("%03d", seq);
    }

    @Override
    public List<CostRecord> getList(Long categoryId, Long batchId, Integer costType,
                                     LocalDate startDate, LocalDate endDate) {
        List<Long> categoryIds = categoryService.getDescendantIds(categoryId);
        List<CostRecord> list = lambdaQuery()
                .in(!categoryIds.isEmpty(), CostRecord::getCategoryId, categoryIds)
                .eq(batchId != null, CostRecord::getBatchId, batchId)
                .eq(costType != null, CostRecord::getCostType, costType)
                .ge(startDate != null, CostRecord::getCostDate, startDate)
                .le(endDate != null, CostRecord::getCostDate, endDate)
                .orderByDesc(CostRecord::getCostDate)
                .orderByDesc(CostRecord::getId)
                .list();

        // populate names
        List<LivestockCategory> categories = categoryService.list();
        List<Batch> batches = batchService.lambdaQuery().select(Batch::getId, Batch::getBatchNo).list();
        for (CostRecord r : list) {
            categories.stream().filter(c -> c.getId().equals(r.getCategoryId())).findFirst()
                    .ifPresent(c -> r.setCategoryName(c.getCategoryName()));
            batches.stream().filter(b -> b.getId().equals(r.getBatchId())).findFirst()
                    .ifPresent(b -> r.setBatchNum(b.getBatchNo()));
        }
        return list;
    }

    /**
     * Save cost record with auto-generated batchNo
     */
    @Override
    public boolean save(CostRecord entity) {
        if (entity.getBatchNo() == null || entity.getBatchNo().isBlank()) {
            entity.setBatchNo(generateBatchNo());
        }
        return super.save(entity);
    }

    @Override
    public String autoCalculateFeedCost() {
        List<Batch> activeBatches = batchService.lambdaQuery().eq(Batch::getStatus, 1).list();
        if (activeBatches.isEmpty()) return "没有存栏批次";

        // load categories
        List<LivestockCategory> categories = categoryService.list();
        Map<Long, LivestockCategory> catMap = categories.stream()
                .collect(Collectors.toMap(LivestockCategory::getId, c -> c));

        LocalDate today = LocalDate.now();
        int created = 0, updated = 0, skipped = 0;

        for (Batch batch : activeBatches) {
            LivestockCategory cat = catMap.get(batch.getCategoryId());
            if (cat == null) continue;
            if (batch.getCurrentQuantity() == null || batch.getCurrentQuantity() <= 0) continue;

            BigDecimal dailyConsumption;
            Integer stage = batch.getCurrentStage();
            if (stage == null) continue;
            switch (stage) {
                case 1: dailyConsumption = cat.getFeedConsumptionSeedling(); break;
                case 2: dailyConsumption = cat.getFeedConsumptionYoung(); break;
                case 3: dailyConsumption = cat.getFeedConsumptionAdult(); break;
                case 4: dailyConsumption = cat.getFeedConsumptionAdult(); break;
                default: continue;
            }
            if (dailyConsumption == null || dailyConsumption.compareTo(BigDecimal.ZERO) <= 0) continue;

            LocalDate entryDate = batch.getEntryDate();
            if (entryDate == null || entryDate.isAfter(today)) continue;
            long days = Math.max(1, ChronoUnit.DAYS.between(entryDate, today));

            BigDecimal feedPrice = cat.getFeedPrice();
            if (feedPrice == null || feedPrice.compareTo(BigDecimal.ZERO) <= 0) {
                feedPrice = new BigDecimal("3.50");
            }

            BigDecimal amount = dailyConsumption
                    .multiply(BigDecimal.valueOf(days))
                    .multiply(BigDecimal.valueOf(batch.getCurrentQuantity()))
                    .multiply(feedPrice)
                    .setScale(2, RoundingMode.HALF_UP);

            // check existing: same batchId + costType(1) + today
            List<CostRecord> existingList = lambdaQuery()
                    .eq(CostRecord::getBatchId, batch.getId())
                    .eq(CostRecord::getCostType, 1)
                    .eq(CostRecord::getCostDate, today)
                    .last("LIMIT 1")
                    .list();
            CostRecord existing = existingList.isEmpty() ? null : existingList.get(0);

            if (existing != null) {
                if (existing.getAmount().compareTo(amount) == 0) {
                    skipped++;
                } else {
                    existing.setAmount(amount);
                    existing.setRemark("自动计算(" + days + "天, " + batch.getCurrentQuantity() + "头, ¥" + feedPrice + "/kg)");
                    updateById(existing);
                    updated++;
                }
            } else {
                CostRecord record = new CostRecord();
                record.setBatchId(batch.getId());
                record.setCategoryId(batch.getCategoryId());
                record.setCostType(1);
                record.setAmount(amount);
                record.setCostDate(today);
                record.setRemark("自动计算(" + days + "天, " + batch.getCurrentQuantity() + "头, ¥" + feedPrice + "/kg)");
                save(record);
                created++;
            }
        }
        return "饲料费：新增" + created + "条，更新" + updated + "条，跳过" + skipped + "条";
    }

    @Override
    public String autoCalculateSimpleCost(int costType, BigDecimal unitCost) {
        if (unitCost == null || unitCost.compareTo(BigDecimal.ZERO) <= 0) return "单价必须大于0";
        String[] typeNames = {"", "", "", "人工费", "水电费", "设备费"};
        if (costType < 3 || costType > 5) return "不支持的成本类型";

        List<Batch> activeBatches = batchService.lambdaQuery().eq(Batch::getStatus, 1).list();
        if (activeBatches.isEmpty()) return "没有存栏批次";

        LocalDate today = LocalDate.now();
        int created = 0, updated = 0, skipped = 0;

        // group by categoryId
        Map<Long, List<Batch>> byCategory = activeBatches.stream()
                .filter(b -> b.getCategoryId() != null && b.getCurrentQuantity() != null && b.getCurrentQuantity() > 0)
                .collect(Collectors.groupingBy(Batch::getCategoryId));

        for (Map.Entry<Long, List<Batch>> entry : byCategory.entrySet()) {
            int quantity = entry.getValue().stream().mapToInt(Batch::getCurrentQuantity).sum();
            BigDecimal amount = unitCost.multiply(BigDecimal.valueOf(quantity))
                    .setScale(2, RoundingMode.HALF_UP);

            // check existing: same categoryId + costType + today
            List<CostRecord> existingList = lambdaQuery()
                    .eq(CostRecord::getCategoryId, entry.getKey())
                    .eq(CostRecord::getCostType, costType)
                    .eq(CostRecord::getCostDate, today)
                    .last("LIMIT 1")
                    .list();
            CostRecord existing = existingList.isEmpty() ? null : existingList.get(0);

            if (existing != null) {
                if (existing.getAmount().compareTo(amount) == 0) {
                    skipped++;
                } else {
                    existing.setAmount(amount);
                    existing.setRemark("自动计算(" + quantity + "头/只, " + typeNames[costType] + " ¥" + unitCost + "/头)");
                    updateById(existing);
                    updated++;
                }
            } else {
                CostRecord record = new CostRecord();
                record.setCategoryId(entry.getKey());
                record.setCostType(costType);
                record.setAmount(amount);
                record.setCostDate(today);
                record.setRemark("自动计算(" + quantity + "头/只, " + typeNames[costType] + " ¥" + unitCost + "/头)");
                save(record);
                created++;
            }
        }
        return typeNames[costType] + "：新增" + created + "条，更新" + updated + "条，跳过" + skipped + "条";
    }

    @Override
    public String autoCalculateMgmtCost() {
        List<Batch> activeBatches = batchService.lambdaQuery()
                .eq(Batch::getStatus, 1).list();
        if (activeBatches.isEmpty()) return "没有存栏批次";

        List<LivestockCategory> categories = categoryService.list();
        Map<Long, LivestockCategory> catMap = categories.stream()
                .collect(Collectors.toMap(LivestockCategory::getId, c -> c));

        LocalDate today = LocalDate.now();
        int created = 0, updated = 0, skipped = 0;

        for (Batch batch : activeBatches) {
            LivestockCategory cat = catMap.get(batch.getCategoryId());
            if (cat == null) continue;
            BigDecimal dailyMgmt = cat.getDailyMgmtFee();
            if (dailyMgmt == null || dailyMgmt.compareTo(BigDecimal.ZERO) <= 0) continue;
            if (batch.getCurrentQuantity() == null || batch.getCurrentQuantity() <= 0) continue;

            BigDecimal amount = dailyMgmt
                    .multiply(BigDecimal.valueOf(batch.getCurrentQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);

            // check existing: same batchId + costType(7) + today
            List<CostRecord> existingList = lambdaQuery()
                    .eq(CostRecord::getBatchId, batch.getId())
                    .eq(CostRecord::getCostType, 7)
                    .eq(CostRecord::getCostDate, today)
                    .last("LIMIT 1")
                    .list();
            CostRecord existing = existingList.isEmpty() ? null : existingList.get(0);

            if (existing != null) {
                if (existing.getAmount().compareTo(amount) == 0) {
                    skipped++;
                } else {
                    existing.setAmount(amount);
                    existing.setRemark("自动计算(1天, " + batch.getCurrentQuantity() + "头, 管理费 ¥" + dailyMgmt + "/头/天)");
                    updateById(existing);
                    updated++;
                }
            } else {
                CostRecord record = new CostRecord();
                record.setBatchId(batch.getId());
                record.setCategoryId(batch.getCategoryId());
                record.setCostType(7);
                record.setAmount(amount);
                record.setCostDate(today);
                record.setRemark("自动计算(1天, " + batch.getCurrentQuantity() + "头, 管理费 ¥" + dailyMgmt + "/头/天)");
                save(record);
                created++;
            }
        }
        return "日常管理费：新增" + created + "条，更新" + updated + "条，跳过" + skipped + "条";
    }

    @Override
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        removeBatchByIds(ids);
    }
}
