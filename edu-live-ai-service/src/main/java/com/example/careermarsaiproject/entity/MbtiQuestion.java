package com.example.careermarsaiproject.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * MBTI题目数据表
 * </p>
 *
 * @author 叶陵
 * @since 2026-07-21
 */
@Getter
@Setter
@TableName("tb_mbti_question")
public class MbtiQuestion implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 维度 1-EI 2-SN 3-TF 4-JP
     */
    private Integer type;

    /**
     * 题目内容
     */
    private String questionContent;

    /**
     * 选项a描述
     */
    private String optionA;

    /**
     * 选项b描述
     */
    private String optionB;

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
