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
 * @since 2026-09-30
 */
@Getter
@Setter
@TableName("yxb_danmu_record")
public class YxbDanmuRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 弹幕内容
     */
    private String context;

    /**
     * 创建时间
     */
    private LocalDateTime creatTime;
}
