package com.livestock.controller;

import com.livestock.common.Result;
import com.livestock.entity.BreedingSite;
import com.livestock.service.BreedingSiteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sites")
public class BreedingSiteController {

    private final BreedingSiteService siteService;

    public BreedingSiteController(BreedingSiteService siteService) {
        this.siteService = siteService;
    }

    @GetMapping
    public Result<List<BreedingSite>> getAll() {
        return Result.ok(siteService.lambdaQuery()
                .orderByAsc(BreedingSite::getId)
                .list());
    }

    @GetMapping("/{id}")
    public Result<BreedingSite> getById(@PathVariable Long id) {
        BreedingSite site = siteService.getById(id);
        if (site == null) {
            return Result.error("场地不存在");
        }
        return Result.ok(site);
    }

    @PostMapping
    public Result<Void> create(@RequestBody BreedingSite site) {
        siteService.save(site);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody BreedingSite site) {
        site.setId(id);
        siteService.updateById(site);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        siteService.removeById(id);
        return Result.ok();
    }
}
