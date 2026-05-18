package com.livestock.controller;

import com.livestock.common.Result;
import com.livestock.entity.CostRecord;
import com.livestock.service.CostRecordService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/costs")
public class CostRecordController {

    private final CostRecordService costService;

    public CostRecordController(CostRecordService costService) {
        this.costService = costService;
    }

    @GetMapping
    public Result<List<CostRecord>> getList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) Integer costType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;
        return Result.ok(costService.getList(categoryId, batchId, costType, start, end));
    }

    @PostMapping
    public Result<Void> create(@RequestBody CostRecord cost) {
        costService.save(cost);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        costService.removeById(id);
        return Result.ok();
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        costService.batchDelete(ids);
        return Result.ok();
    }

    @PostMapping("/auto-feed")
    public Result<String> autoCalculateFeedCost() {
        return Result.ok(costService.autoCalculateFeedCost());
    }

    @PostMapping("/auto-mgmt")
    public Result<String> autoCalculateMgmtCost() {
        return Result.ok(costService.autoCalculateMgmtCost());
    }

    @PostMapping("/auto-calc")
    public Result<String> autoCalculateSimpleCost(@RequestParam int costType, @RequestParam BigDecimal unitCost) {
        return Result.ok(costService.autoCalculateSimpleCost(costType, unitCost));
    }
}
