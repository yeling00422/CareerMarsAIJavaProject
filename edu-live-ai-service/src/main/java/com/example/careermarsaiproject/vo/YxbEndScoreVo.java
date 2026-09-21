package com.example.careermarsaiproject.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "YxbEndScoreVo", description = "毓秀杯最终得分")
public class YxbEndScoreVo {
    private String name;
    private Double expertEndScore;
    private Double volkswagenEndScore;
    private Double endScore;
}