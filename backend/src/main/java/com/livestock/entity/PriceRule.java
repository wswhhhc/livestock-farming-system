package com.livestock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("price_rule")
public class PriceRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long categoryId;
    private String ruleName;
    private Integer direction;
    private BigDecimal ratio;
    private String applyCondition;
    private Integer priority;
    private Integer enabled;

    @TableField(exist = false)
    private String categoryName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
