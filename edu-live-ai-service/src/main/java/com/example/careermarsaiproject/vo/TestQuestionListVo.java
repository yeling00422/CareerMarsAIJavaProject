package com.example.careermarsaiproject.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "TestQuestionListVo", description = "问题列表")
public class TestQuestionListVo {
    private List<TestQuestionVo> questions;
}