package com.example.careermarsaiproject.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(value = "ResumeVo", description = "返回简历对象")
public class ResumeVo {
    private String name;
    private String date;
    private String educationalQualifications;
    private String school;
    private String educationalTime;
    private String skill;
    private String educationalExperience;
    private String jobExperience;
}