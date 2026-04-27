package com.example.careermarsaiproject.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MentorVo{

    private static final long serialVersionUID = 1L;
    /**
     * 导师id
     */
    private String id;

    /**
     * 导师名称
     */
    private String menName;

    /**
     * 毕业院校
     */
    private String schoolName;

    /**
     * 任职企业
     */
    private String workEnterprise;

    /**
     * 领域名称
     */
    private String lableNames;

    private Integer studyCount;

}
