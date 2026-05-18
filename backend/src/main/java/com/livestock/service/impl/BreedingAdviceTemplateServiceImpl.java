package com.livestock.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.livestock.entity.BreedingAdviceTemplate;
import com.livestock.entity.LivestockCategory;
import com.livestock.mapper.BreedingAdviceTemplateMapper;
import com.livestock.service.BreedingAdviceTemplateService;
import com.livestock.service.LivestockCategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BreedingAdviceTemplateServiceImpl
        extends ServiceImpl<BreedingAdviceTemplateMapper, BreedingAdviceTemplate>
        implements BreedingAdviceTemplateService {

    private final LivestockCategoryService categoryService;

    public BreedingAdviceTemplateServiceImpl(LivestockCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public List<BreedingAdviceTemplate> getList(Long categoryId) {
        List<Long> categoryIds = categoryService.getDescendantIds(categoryId);
        List<BreedingAdviceTemplate> list = lambdaQuery()
                .in(!categoryIds.isEmpty(), BreedingAdviceTemplate::getCategoryId, categoryIds)
                .orderByAsc(BreedingAdviceTemplate::getSortOrder)
                .list();

        List<LivestockCategory> categories = categoryService.list();
        for (BreedingAdviceTemplate t : list) {
            categories.stream().filter(c -> c.getId().equals(t.getCategoryId())).findFirst()
                    .ifPresent(c -> t.setCategoryName(c.getCategoryName()));
        }
        return list;
    }
}
