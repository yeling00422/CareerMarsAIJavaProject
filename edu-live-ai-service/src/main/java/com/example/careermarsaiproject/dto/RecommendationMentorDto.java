package com.example.careermarsaiproject.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "AnalysisResultDto", description = "分析结果响应")
public class RecommendationMentorDto {
    @ApiModelProperty(value = "简历")
    private String resumeText;
    @ApiModelProperty(value = "岗位")
    private String position;
}
