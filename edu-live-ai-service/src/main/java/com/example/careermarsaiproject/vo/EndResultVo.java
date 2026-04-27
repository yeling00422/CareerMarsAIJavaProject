package com.example.careermarsaiproject.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "EndResultVo", description = "匹配最终结果")
public class EndResultVo {
    private List<MentorResultVo> mentorList;
}
