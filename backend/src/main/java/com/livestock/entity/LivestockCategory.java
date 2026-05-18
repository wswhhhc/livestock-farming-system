package com.livestock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("livestock_category")
public class LivestockCategory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    private String categoryName;
    private Integer growthCycle;
    private java.math.BigDecimal slaughterWeight;
    private Integer slaughterMonthAge;
    private java.math.BigDecimal feedConsumptionSeedling;
    private java.math.BigDecimal feedConsumptionYoung;
    private java.math.BigDecimal feedConsumptionAdult;
    private String feedType;
    private String commonDiseases;
    private Integer breedMode;
    private java.math.BigDecimal basePrice;
    private java.math.BigDecimal feedPrice;
    private java.math.BigDecimal dailyMgmtFee;
    private java.math.BigDecimal estimatedSlaughterRate;
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private List<LivestockCategory> children;
}
