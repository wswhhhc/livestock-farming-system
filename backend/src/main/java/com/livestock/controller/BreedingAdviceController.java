package com.livestock.controller;

import com.livestock.common.Result;
import com.livestock.entity.BreedingAdvice;
import com.livestock.entity.BreedingAdviceTemplate;
import com.livestock.service.BreedingAdviceService;
import com.livestock.service.BreedingAdviceTemplateService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/advice")
public class BreedingAdviceController {

    private final BreedingAdviceService adviceService;
    private final BreedingAdviceTemplateService templateService;

    public BreedingAdviceController(BreedingAdviceService adviceService,
                                    BreedingAdviceTemplateService templateService) {
        this.adviceService = adviceService;
        this.templateService = templateService;
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

    // templates
    @GetMapping("/templates")
    public Result<List<BreedingAdviceTemplate>> getTemplates(
            @RequestParam(required = false) Long categoryId) {
        return Result.ok(templateService.getList(categoryId));
    }

    @PostMapping("/templates")
    public Result<Void> createTemplate(@RequestBody BreedingAdviceTemplate template) {
        templateService.save(template);
        return Result.ok();
    }

    @PutMapping("/templates/{id}")
    public Result<Void> updateTemplate(@PathVariable Long id, @RequestBody BreedingAdviceTemplate template) {
        template.setId(id);
        templateService.updateById(template);
        return Result.ok();
    }

    @DeleteMapping("/templates/{id}")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        templateService.removeById(id);
        return Result.ok();
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.ok(adviceService.lambdaQuery().eq(BreedingAdvice::getIsRead, 0).count());
    }
}
