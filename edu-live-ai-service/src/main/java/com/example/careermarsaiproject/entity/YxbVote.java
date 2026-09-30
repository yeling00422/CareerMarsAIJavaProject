package com.example.careermarsaiproject.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author 叶陵
 * @since 2026-09-22
 */
@Getter
@Setter
@TableName("yxb_vote")
public class YxbVote implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 姓名
     */
    private String name;

    /**
     * 头像
     */
    private String headImage;

    /**
     * 作品
     */
    private String work;

    /**
     * 组名
     */
    private String groupName;

    /**
     * 票数
     */
    private Integer voteNumber;
}
