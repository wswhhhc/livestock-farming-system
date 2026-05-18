package com.livestock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("breeding_advice_template")
public class BreedingAdviceTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long categoryId;
    private Integer growthStage;
    private String adviceType;
    private String adviceContent;
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String categoryName;
}
