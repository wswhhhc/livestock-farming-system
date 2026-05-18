package com.livestock.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.livestock.entity.LivestockCategory;
import com.livestock.entity.PriceRule;
import com.livestock.mapper.PriceRuleMapper;
import com.livestock.service.LivestockCategoryService;
import com.livestock.service.PriceRuleService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PriceRuleServiceImpl extends ServiceImpl<PriceRuleMapper, PriceRule>
        implements PriceRuleService {

    private final LivestockCategoryService categoryService;

    public PriceRuleServiceImpl(LivestockCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public List<PriceRule> getList(Long categoryId, Integer enabled) {
        List<Long> categoryIds = categoryService.getDescendantIds(categoryId);
        List<PriceRule> list = lambdaQuery()
                .in(!categoryIds.isEmpty(), PriceRule::getCategoryId, categoryIds)
                .eq(enabled != null, PriceRule::getEnabled, enabled)
                .orderByAsc(PriceRule::getPriority)
                .orderByDesc(PriceRule::getId)
                .list();

        List<LivestockCategory> categories = categoryService.list();
        for (PriceRule r : list) {
            categories.stream().filter(c -> c.getId().equals(r.getCategoryId())).findFirst()
                    .ifPresent(c -> r.setCategoryName(c.getCategoryName()));
        }
        return list;
    }
}
