package com.livestock.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.livestock.entity.Batch;

import java.util.List;

public interface BatchService extends IService<Batch> {
    String generateBatchNo(Long categoryId);
    List<Batch> getList(Long categoryId, Integer status, String keyword);
}
