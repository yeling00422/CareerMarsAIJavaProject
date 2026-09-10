package com.example.careermarsaiproject.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 毓秀杯ai评分数据
 * </p>
 *
 * @author 叶陵
 * @since 2026-09-09
 */
@Getter
@Setter
@TableName("yxb_score")
public class YxbScore implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 选手名称
     */
    private String name;

    /**
     * 作品名称
     */
    private String work;

    /**
     * 影视内涵得分
     */
    private Double score1;

    /**
     * 语言语调得分
     */
    private Double score2;

    /**
     * 情感表达得分
     */
    private Double score3;

    /**
     * 角色还原度得分
     */
    private Double score4;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
