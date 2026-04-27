package com.example.careermarsaiproject.dto;

import com.example.careermarsaiproject.vo.RecommendedPosition;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "CharacteristicsTestReportDto", description = "性格测试报告传参")
public class CharacteristicsTestReportDto {
        private String characteristicsTest;
        private String userAnswers;
        private String resumeText;
}
