package com.livestock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("cost_record")
public class CostRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String batchNo;
    private Long batchId;
    private Long categoryId;
    private Integer costType;
    private BigDecimal amount;
    private LocalDate costDate;
    private String remark;

    @TableField(exist = false)
    private String categoryName;

    @TableField(exist = false)
    private String batchNum;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
