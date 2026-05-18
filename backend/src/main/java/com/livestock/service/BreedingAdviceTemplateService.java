package com.livestock.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.livestock.entity.BreedingAdviceTemplate;

import java.util.List;

public interface BreedingAdviceTemplateService extends IService<BreedingAdviceTemplate> {
    List<BreedingAdviceTemplate> getList(Long categoryId);
}
