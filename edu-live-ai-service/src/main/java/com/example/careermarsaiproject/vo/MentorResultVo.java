package com.example.careermarsaiproject.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
@Data
public class MentorResultVo {
    private String id;
    private int marryRate;
    @JsonProperty("menName")
    private String name;
    private String lableNames;
    private String resume;
    private int level;
    @JsonProperty("schoolName")
    private String schoolName;
    @JsonProperty("workEnterprise")
    private String workEnterprise;
    private double rating;
    @JsonProperty("studyCount")
    private int studentCount;
    @JsonProperty("successRate")
    private int placementRate;
    private List<String> reasons;
}
