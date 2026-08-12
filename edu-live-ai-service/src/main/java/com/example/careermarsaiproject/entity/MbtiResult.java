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
     * 核心优势
     */
    private String advantage;

    /**
     * 明显短板
     */
    private String disadvantage;

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
     * 个人成长 & 职场建议
     */
    private String suggestion;

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
