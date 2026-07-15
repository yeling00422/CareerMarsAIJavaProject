package com.example.careermarsaiproject.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "MBTIOptionVo", description = "性格测试题选项")
public class MBTIOptionVo {
    @ApiModelProperty(value = "选项")
    private String option;

    @ApiModelProperty(value = "选项描述")
    private String text;

}
