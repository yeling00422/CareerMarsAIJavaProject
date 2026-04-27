package com.example.careermarsaiproject.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(value = "PersonalAbility", description = "个人能力")
public class PersonalAbility {
    private String ability;//能力
    private Integer score;//得分
}
