package com.livestock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("breeding_advice")
public class BreedingAdvice {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long batchId;
    private Long templateId;
    private Integer growthStage;
    private String adviceContent;
    private Integer isRead;
    private Integer triggerType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String batchNo;

    @TableField(exist = false)
    private String categoryName;
}
