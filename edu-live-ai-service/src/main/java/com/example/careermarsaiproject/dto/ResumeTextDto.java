package com.example.careermarsaiproject.dto;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "ResumeTextDto", description = "简历")
public class ResumeTextDto {
    private String resumeText;
}