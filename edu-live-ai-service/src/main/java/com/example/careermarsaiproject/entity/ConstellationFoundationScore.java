package com.example.careermarsaiproject.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 星座基础分
 * </p>
 *
 * @author 叶陵
 * @since 2026-07-21
 */
@Getter
@Setter
@TableName("tb_constellation_foundation_score")
public class ConstellationFoundationScore implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 星座
     */
    private String constellation;

    /**
     * 外向E基础分
     */
    private Integer eScore;

    /**
     * 内向基础分
     */
    private Integer iScore;

    /**
     * 实感基础分
     */
    private Integer sScore;

    /**
     * 直觉基础分
     */
    private Integer nScore;

    /**
     * 理性基础分
     */
    private Integer tScore;

    /**
     * 感性基础分
     */
    private Integer fScore;

    /**
     * 计划基础分
     */
    private Integer jScore;

    /**
     * 随性基础分
     */
    private Integer pScore;

    /**
     * 属性
     */
    private String attributes;

    /**
     * 先天特质说明
     */
    private String explanation;

    /**
     * 创建人id
     */
    private String createId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人id
     */
    private String updateId;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
