package com.livestock.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.livestock.entity.PriceRule;

import java.util.List;

public interface PriceRuleService extends IService<PriceRule> {
    List<PriceRule> getList(Long categoryId, Integer enabled);
}
