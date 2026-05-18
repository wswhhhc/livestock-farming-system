package com.livestock.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.livestock.entity.Batch;
import com.livestock.entity.BreedingSite;
import com.livestock.entity.LivestockCategory;
import com.livestock.mapper.BatchMapper;
import com.livestock.service.BatchService;
import com.livestock.service.BreedingSiteService;
import com.livestock.service.LivestockCategoryService;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class BatchServiceImpl extends ServiceImpl<BatchMapper, Batch> implements BatchService {

    private final LivestockCategoryService categoryService;
    private final BreedingSiteService siteService;

    public BatchServiceImpl(LivestockCategoryService categoryService, BreedingSiteService siteService) {
        this.categoryService = categoryService;
        this.siteService = siteService;
    }

    @Override
    public String generateBatchNo(Long categoryId) {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "P" + datePart;

        Batch last = baseMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Batch>()
                        .select(Batch::getBatchNo)
                        .eq(Batch::getDeleted, 0)
                        .likeRight(Batch::getBatchNo, prefix)
                        .orderByDesc(Batch::getBatchNo)
                        .last("LIMIT 1"));

        int seq = 1;
        if (last != null) {
            seq = Integer.parseInt(last.getBatchNo().substring(prefix.length())) + 1;
        }
        return prefix + String.format("%03d", seq);
    }

    @Override
    public boolean removeById(Serializable id) {
        return baseMapper.logicDeleteById((Long) id) > 0;
    }

    @Override
    public List<Batch> getList(Long categoryId, Integer status, String keyword) {
        List<Long> categoryIds = categoryService.getDescendantIds(categoryId);
        List<Batch> list = lambdaQuery()
                .in(!categoryIds.isEmpty(), Batch::getCategoryId, categoryIds)
                .eq(status != null, Batch::getStatus, status)
                .and(StringUtils.isNotBlank(keyword), w -> w
                        .like(Batch::getBatchNo, keyword)
                        .or()
                        .like(Batch::getResponsiblePerson, keyword))
                .orderByDesc(Batch::getId)
                .list();

        // populate category name and site name
        List<LivestockCategory> categories = categoryService.list();
        List<BreedingSite> sites = siteService.list();
        for (Batch b : list) {
            categories.stream().filter(c -> c.getId().equals(b.getCategoryId())).findFirst()
                    .ifPresent(c -> b.setCategoryName(c.getCategoryName()));
            sites.stream().filter(s -> s.getId().equals(b.getSiteId())).findFirst()
                    .ifPresent(s -> b.setSiteName(s.getSiteName()));
        }
        return list;
    }
}
