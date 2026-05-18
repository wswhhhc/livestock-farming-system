package com.livestock.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.livestock.entity.LivestockCategory;
import com.livestock.mapper.LivestockCategoryMapper;
import com.livestock.service.LivestockCategoryService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LivestockCategoryServiceImpl extends ServiceImpl<LivestockCategoryMapper, LivestockCategory>
        implements LivestockCategoryService {

    @Override
    public List<LivestockCategory> getTree() {
        List<LivestockCategory> all = lambdaQuery()
                .orderByAsc(LivestockCategory::getSortOrder)
                .list();

        Map<Long, List<LivestockCategory>> parentMap = all.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(LivestockCategory::getParentId));

        List<LivestockCategory> roots = new ArrayList<>();
        for (LivestockCategory c : all) {
            if (c.getParentId() == null) {
                c.setChildren(parentMap.getOrDefault(c.getId(), new ArrayList<>()));
                roots.add(c);
            }
        }
        return roots;
    }

    @Override
    public List<Long> getDescendantIds(Long categoryId) {
        if (categoryId == null) return List.of();

        List<LivestockCategory> all = lambdaQuery()
                .select(LivestockCategory::getId, LivestockCategory::getParentId)
                .list();

        Map<Long, List<Long>> parentMap = new HashMap<>();
        for (LivestockCategory c : all) {
            if (c.getParentId() != null) {
                parentMap.computeIfAbsent(c.getParentId(), k -> new ArrayList<>()).add(c.getId());
            }
        }

        List<Long> result = new ArrayList<>();
        Deque<Long> stack = new ArrayDeque<>();
        stack.push(categoryId);
        while (!stack.isEmpty()) {
            Long id = stack.pop();
            result.add(id);
            List<Long> children = parentMap.get(id);
            if (children != null) {
                children.forEach(stack::push);
            }
        }
        return result;
    }
}
