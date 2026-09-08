package com.example.careermarsaiproject.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * Mbti人格测试最终结果
 * </p>
 *
 * @author 叶陵
 * @since 2026-07-21
 */
@Getter
@Setter
@TableName("tb_mbti_result")
public class MbtiResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * mbti名称
     */
    private String name;

    /**
     * 分组
     */
    @TableField("`group`")
    private String group;

    /**
     * 人格类型
     */
    private String type;

    /**
     * 完整性格描述
     */
    private String description;

    /**
     * 核心优势1
     */
    private String advantage1;

    /**
     * 核心优势2
     */
    private String advantage2;

    /**
     * 核心优势3
     */
    private String advantage3;

    /**
     * 核心优势4
     */
    private String advantage4;

    /**
     * 明显短板1
     */
    private String disadvantage1;

    /**
     * 明显短板2
     */
    private String disadvantage2;

    /**
     * 明显短板3
     */
    private String disadvantage3;

    /**
     * 明显短板4
     */
    private String disadvantage4;

    /**
     * 首选行业
     */
    private String firstIndustry;

    /**
     * 首选岗位
     */
    private String firstPostion;

    /**
     * 次选行业
     */
    private String secondIndustry;

    /**
     * 次选岗位
     */
    private String secondPostion;

    /**
     * 尽量避开
     */
    private String avoid;

    /**
     * 个人成长 & 职场建议1
     */
    private String suggestion1;

    /**
     * 个人成长 & 职场建议2
     */
    private String suggestion2;

    /**
     * 个人成长 & 职场建议3
     */
    private String suggestion3;

    /**
     * 个人成长 & 职场建议4
     */
    private String suggestion4;

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
