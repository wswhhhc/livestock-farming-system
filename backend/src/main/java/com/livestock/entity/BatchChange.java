package com.livestock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("batch_change")
public class BatchChange {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long batchId;
    private Integer changeType;
    private Integer changeQuantity;
    private LocalDate changeDate;
    private Integer quantityAfterChange;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
