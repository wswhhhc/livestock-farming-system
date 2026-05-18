package com.livestock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("batch")
public class Batch {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String batchNo;
    private Long categoryId;
    private Integer currentStage;
    private Integer initialQuantity;
    private Integer currentQuantity;
    private LocalDate entryDate;
    private LocalDate expectedSlaughterDate;
    private Long siteId;
    private String responsiblePerson;
    private Integer status;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String categoryName;

    @TableField(exist = false)
    private String siteName;
}
