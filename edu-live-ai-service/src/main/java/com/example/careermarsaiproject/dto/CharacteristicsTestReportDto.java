package com.example.careermarsaiproject.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(value = "CharacteristicsTestReportDto", description = "性格测试报告传参")
public class CharacteristicsTestReportDto {
        private String characteristicsTest;
        private String userAnswers;
        private String resumeText;
}
