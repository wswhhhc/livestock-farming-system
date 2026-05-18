package com.livestock.controller;

import com.livestock.common.Result;
import com.livestock.entity.PriceRule;
import com.livestock.service.PriceRuleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/price-rules")
public class PriceRuleController {

    private final PriceRuleService priceRuleService;

    public PriceRuleController(PriceRuleService priceRuleService) {
        this.priceRuleService = priceRuleService;
    }

    @GetMapping
    public Result<List<PriceRule>> getList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer enabled) {
        return Result.ok(priceRuleService.getList(categoryId, enabled));
    }

    @PostMapping
    public Result<Void> create(@RequestBody PriceRule rule) {
        priceRuleService.save(rule);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody PriceRule rule) {
        rule.setId(id);
        priceRuleService.updateById(rule);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        priceRuleService.removeById(id);
        return Result.ok();
    }
}
