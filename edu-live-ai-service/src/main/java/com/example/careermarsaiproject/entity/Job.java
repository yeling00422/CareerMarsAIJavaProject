package com.example.careermarsaiproject.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 岗位数据表
 * </p>
 *
 * @author yeling
 * @since 2026-01-27
 */
@Getter
@Setter
@TableName("tb_job")
public class Job implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 行业id
     */
    private String industryId;

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
     * 最高薪资
     */
    private Integer salaryMin;

    /**
     * 最低薪资
     */
    private Integer salaryMax;

    /**
     * 薪资币种
     */
    private String salaryCurrency;

    /**
     * 链接
     */
    private String jdLink;

    /**
     * 岗位详情
     */
    private String jdText;

    /**
     * 标签
     */
    private String tags;

    /**
     * 申请状态
     */
    private Integer applicationStatus;

    /**
     * 机会状态
     */
    private Integer opportunityStatus;

    /**
     * 最后期限
     */
    private String deadline;

    /**
     * 面试笔记
     */
    private String interviewNotes;

    /**
     * 注释
     */
    private String notes;

    /**
     * 是否活跃 0-否  1-是
     */
    private Integer isActive;

    /**
     * 状态  0-未启用  1-启用
     */
    private Integer status;

    /**
     * 创建人
     */
    private String createId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateId;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
