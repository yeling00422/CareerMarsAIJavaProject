package com.example.careermarsaiproject.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author 叶陵
 * @since 2026-09-24
 */
@Getter
@Setter
@TableName("yxb_vote_record")
public class YxbVoteRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String voteId;

    private String userId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
