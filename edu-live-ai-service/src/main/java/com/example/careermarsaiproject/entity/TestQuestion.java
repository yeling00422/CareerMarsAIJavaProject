package com.example.careermarsaiproject.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 面试题数据表
 * </p>
 *
 * @author 叶陵
 * @since 2026-03-10
 */
@Getter
@Setter
@TableName("tb_written_test_question")
public class TestQuestion implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 岗位id
     */
    private String jobId;

    /**
     * 试卷名称
     */
    private String name;

    /**
     * 类型  1-单选题  2-多选题  3-判断题  4-填空题
     */
    private Integer type;

    /**
     * 题目
     */
    private String title;

    /**
     * 字段1
     */
    private String field1;

    /**
     * 字段2
     */
    private String field2;

    /**
     * 字段3
     */
    private String field3;

    /**
     * 字段4
     */
    private String field4;

    /**
     * 字段5
     */
    private String field5;

    /**
     * 字段6
     */
    private String field6;

    /**
     * 答案
     */
    private String answer;

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
