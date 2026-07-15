package com.example.careermarsaiproject.vo;
import lombok.Data;



@Data
public class JobVo{
    private String id;

    /**
     * 公司名称
     */
    private String company;

    /**
     * 岗位名称
     */
    private String position;

    /**
     * 工作地点
     */
    private String locations;

    /**
     * 岗位详情
     */
    private String jdText;
}
