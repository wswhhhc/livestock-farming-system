package com.livestock.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.livestock.entity.CostRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface CostRecordService extends IService<CostRecord> {
    List<CostRecord> getList(Long categoryId, Long batchId, Integer costType, LocalDate startDate, LocalDate endDate);
    String autoCalculateFeedCost();
    String autoCalculateSimpleCost(int costType, BigDecimal unitCost);
    String autoCalculateMgmtCost();
    void batchDelete(List<Long> ids);
}
