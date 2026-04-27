package com.example.careermarsaiproject.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;
@Data
@ApiModel(value = "ResumeVo", description = "返回简历对象")
public class ResumeVo {
    private String name;
    private String educationalQualifications;
    private String skill;
    private String educationalExperience;
    private String jobExperience;
}