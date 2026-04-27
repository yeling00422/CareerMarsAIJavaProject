package com.example.careermarsaiproject.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "MatchPositionVo", description = "匹配岗位结果")
public class MatchJobVo {
    @ApiModelProperty(value = "岗位id")
    private String id;
    @ApiModelProperty(value = "公司名称")
    private String company;
    @ApiModelProperty(value = "岗位名称")
    private String position;
}
