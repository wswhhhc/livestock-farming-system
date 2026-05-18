package com.livestock.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.livestock.entity.BreedingAdvice;

import java.util.List;

public interface BreedingAdviceService extends IService<BreedingAdvice> {
    List<BreedingAdvice> getList(Long categoryId, Integer isRead);
    void generate();
    void markRead(Long id);
}
