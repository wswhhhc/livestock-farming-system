package com.livestock.controller;

import com.livestock.common.Result;
import com.livestock.entity.BreedingAdvice;
import com.livestock.service.BreedingAdviceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/advice")
public class BreedingAdviceController {

    private final BreedingAdviceService adviceService;

    public BreedingAdviceController(BreedingAdviceService adviceService) {
        this.adviceService = adviceService;
    }

    @GetMapping
    public Result<List<BreedingAdvice>> getList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer isRead) {
        return Result.ok(adviceService.getList(categoryId, isRead));
    }

    @PostMapping("/generate")
    public Result<Void> generate() {
        adviceService.generate();
        return Result.ok();
    }

    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        adviceService.markRead(id);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adviceService.removeById(id);
        return Result.ok();
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.ok(adviceService.lambdaQuery().eq(BreedingAdvice::getIsRead, 0).count());
    }
}
