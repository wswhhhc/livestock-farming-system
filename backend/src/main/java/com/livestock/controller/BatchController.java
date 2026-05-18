package com.livestock.controller;

import com.livestock.common.Result;
import com.livestock.entity.Batch;
import com.livestock.entity.BatchChange;
import com.livestock.service.BatchChangeService;
import com.livestock.service.BatchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/batches")
public class BatchController {

    private final BatchService batchService;
    private final BatchChangeService batchChangeService;

    public BatchController(BatchService batchService, BatchChangeService batchChangeService) {
        this.batchService = batchService;
        this.batchChangeService = batchChangeService;
    }

    @GetMapping
    public Result<List<Batch>> getList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return Result.ok(batchService.getList(categoryId, status, keyword));
    }

    @GetMapping("/{id}")
    public Result<Batch> getById(@PathVariable Long id) {
        Batch batch = batchService.getById(id);
        return batch != null ? Result.ok(batch) : Result.error("批次不存在");
    }

    @GetMapping("/next-no")
    public Result<String> getNextBatchNo(@RequestParam Long categoryId) {
        return Result.ok(batchService.generateBatchNo(categoryId));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Batch batch) {
        if (batch.getCurrentQuantity() == null) {
            batch.setCurrentQuantity(batch.getInitialQuantity());
        }
        batchService.save(batch);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Batch batch) {
        batch.setId(id);
        batchService.updateById(batch);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        batchService.removeById(id);
        return Result.ok();
    }

    @GetMapping("/{batchId}/changes")
    public Result<List<BatchChange>> getChanges(@PathVariable Long batchId) {
        return Result.ok(batchChangeService.lambdaQuery()
                .eq(BatchChange::getBatchId, batchId)
                .orderByDesc(BatchChange::getId)
                .list());
    }

    @PostMapping("/{batchId}/changes")
    public Result<Void> addChange(@PathVariable Long batchId, @RequestBody BatchChange change) {
        change.setBatchId(batchId);
        if (change.getChangeDate() == null) {
            change.setChangeDate(java.time.LocalDate.now());
        }
        batchChangeService.save(change);
        return Result.ok();
    }

    @DeleteMapping("/{batchId}/changes/{changeId}")
    public Result<Void> deleteChange(@PathVariable Long changeId) {
        batchChangeService.removeById(changeId);
        return Result.ok();
    }
}
