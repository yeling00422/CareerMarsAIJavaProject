package com.example.careermarsaiproject.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 毓秀杯评委
 * </p>
 *
 * @author 叶陵
 * @since 2026-09-10
 */
@Getter
@Setter
@TableName("yxb_judges")
public class YxbJudges implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * 账号
     */
    private String accountNumber;

    /**
     * 密码
     */
    private String password;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
