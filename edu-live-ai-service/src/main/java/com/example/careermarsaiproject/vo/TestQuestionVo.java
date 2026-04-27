package com.example.careermarsaiproject.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "TestQuestionVo", description = "测试问题")
public class TestQuestionVo {
//    @ApiModelProperty(value = "id")
//    private String id;

    @ApiModelProperty(value = "试卷名")
    private String name;

    @ApiModelProperty(value = "题目")
    private String title;

    @ApiModelProperty(value = "类型")
    private Integer type;

    @ApiModelProperty(value = "选项")
    private List<TestQuestionOptionVo> options;

    @ApiModelProperty(value = "答案")
    private String answer;
}
