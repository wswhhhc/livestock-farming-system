package com.livestock.controller;

import com.livestock.common.Result;
import com.livestock.entity.LivestockCategory;
import com.livestock.service.LivestockCategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class LivestockCategoryController {

    private final LivestockCategoryService categoryService;

    public LivestockCategoryController(LivestockCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/tree")
    public Result<List<LivestockCategory>> getTree() {
        return Result.ok(categoryService.getTree());
    }

    @GetMapping
    public Result<List<LivestockCategory>> getAll() {
        return Result.ok(categoryService.lambdaQuery()
                .orderByAsc(LivestockCategory::getSortOrder)
                .list());
    }

    @GetMapping("/{id}")
    public Result<LivestockCategory> getById(@PathVariable Long id) {
        LivestockCategory category = categoryService.getById(id);
        if (category == null) {
            return Result.error("种类不存在");
        }
        return Result.ok(category);
    }

    @PostMapping
    public Result<Void> create(@RequestBody LivestockCategory category) {
        categoryService.save(category);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody LivestockCategory category) {
        category.setId(id);
        categoryService.updateById(category);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        // 获取所有子分类ID，一并删除
        List<Long> ids = categoryService.getDescendantIds(id);
        categoryService.removeByIds(ids);
        return Result.ok();
    }
}
