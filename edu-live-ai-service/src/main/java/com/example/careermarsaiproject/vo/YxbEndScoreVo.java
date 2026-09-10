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
    private int expertScore;
    private int volkswagenScore;
    private int endScore;
}