package com.example.careermarsaiproject.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

@Data
@ApiModel(value = "MBTITestQuestionVo", description = "mbti测试题")
public class MBTITestQuestionVo {
    @ApiModelProperty(value = "题目")
    private String title;

    @ApiModelProperty(value = "选项")
    private List<MBTIOptionVo> options;
}
