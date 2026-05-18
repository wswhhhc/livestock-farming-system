package com.livestock.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.livestock.entity.LivestockCategory;

import java.util.List;

public interface LivestockCategoryService extends IService<LivestockCategory> {
    List<LivestockCategory> getTree();

    /**
     * 获取指定分类及其所有子孙分类的 ID 列表
     */
    List<Long> getDescendantIds(Long categoryId);
}
