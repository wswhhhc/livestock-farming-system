package com.livestock.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.livestock.entity.BreedingSite;
import com.livestock.mapper.BreedingSiteMapper;
import com.livestock.service.BreedingSiteService;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
public class BreedingSiteServiceImpl extends ServiceImpl<BreedingSiteMapper, BreedingSite>
        implements BreedingSiteService {

    @Override
    public boolean removeById(Serializable id) {
        return lambdaUpdate()
                .eq(BreedingSite::getId, id)
                .set(BreedingSite::getDeleted, id)
                .update();
    }
}
