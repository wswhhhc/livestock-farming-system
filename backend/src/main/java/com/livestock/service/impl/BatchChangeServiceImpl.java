package com.livestock.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.livestock.entity.Batch;
import com.livestock.entity.BatchChange;
import com.livestock.mapper.BatchChangeMapper;
import com.livestock.service.BatchChangeService;
import com.livestock.service.BatchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BatchChangeServiceImpl extends ServiceImpl<BatchChangeMapper, BatchChange>
        implements BatchChangeService {

    private final BatchService batchService;

    public BatchChangeServiceImpl(BatchService batchService) {
        this.batchService = batchService;
    }

    @Override
    @Transactional
    public boolean save(BatchChange change) {
        Batch batch = batchService.getById(change.getBatchId());
        if (batch == null) return false;

        int delta;
        if (change.getChangeType() == 4 || change.getChangeType() == 5) {
            // 补栏、转入 → 增加存栏
            delta = change.getChangeQuantity();
        } else {
            // 出栏、死亡、转群、转出 → 减少存栏
            delta = -change.getChangeQuantity();
        }
        int newQuantity = batch.getCurrentQuantity() + delta;
        change.setQuantityAfterChange(newQuantity);
        super.save(change);
        batchService.lambdaUpdate()
                .eq(Batch::getId, change.getBatchId())
                .set(Batch::getCurrentQuantity, newQuantity)
                .update();
        return true;
    }
}
