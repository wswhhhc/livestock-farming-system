package com.livestock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.livestock.entity.Batch;
import org.apache.ibatis.annotations.Update;

public interface BatchMapper extends BaseMapper<Batch> {
    @Update("UPDATE batch SET deleted = #{id} WHERE id = #{id} AND deleted = 0")
    int logicDeleteById(Long id);
}
