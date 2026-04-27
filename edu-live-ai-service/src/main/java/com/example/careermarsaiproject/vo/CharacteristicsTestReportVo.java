package com.example.careermarsaiproject.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "CharacteristicsTestReportListVo", description = "性格测试报告")
public class CharacteristicsTestReportVo {
        private String testReportText;
        private List<RecommendedPosition> recommendedPositionList;
}
