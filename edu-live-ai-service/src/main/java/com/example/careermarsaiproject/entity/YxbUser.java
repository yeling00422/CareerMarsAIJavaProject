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
 * @since 2026-09-23
 */
@Getter
@Setter
@TableName("yxb_user")
public class YxbUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * openId
     */
    private String openId;

    /**
     * unionId
     */
    private String unionId;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 国家数字号码
     */
    private String countryNum;

    /**
     * 国家区域号码
     */
    private String countryCode;

    /**
     * 邮箱号
     */
    private String email;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 头像
     */
    private String headImg;

    /**
     * 性别
     */
    private Integer sex;

    /**
     * 省份
     */
    private String provice;

    /**
     * 城市
     */
    private String city;

    /**
     * 国家
     */
    private String country;

    /**
     * 票数
     */
    private Integer voteCount;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
