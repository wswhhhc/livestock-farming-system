package com.livestock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("breeding_site")
public class BreedingSite {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String siteCode;
    private String siteName;
    private String location;
    private Integer capacity;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
