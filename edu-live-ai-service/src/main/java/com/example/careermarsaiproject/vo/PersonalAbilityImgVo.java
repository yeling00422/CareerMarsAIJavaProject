package com.example.careermarsaiproject.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "PersonalAbilityImgVo", description = "个人能力图")
public class PersonalAbilityImgVo {
    @ApiModelProperty(value = "个人能力")
    private List<PersonalAbility> personalAbility;
}
