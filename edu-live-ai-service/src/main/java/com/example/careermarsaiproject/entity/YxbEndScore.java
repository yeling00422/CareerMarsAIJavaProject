package com.example.careermarsaiproject.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 毓秀杯最终打分表
 * </p>
 *
 * @author 叶陵
 * @since 2026-09-09
 */
@Getter
@Setter
@TableName("yxb_end_score")
public class YxbEndScore implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 选手名称
     */
    private String name;

    /**
     * 专家评委评分总分
     */
    private Integer expert;

    /**
     * 大众评委评分总分
     */
    private Integer volkswagen;
}
