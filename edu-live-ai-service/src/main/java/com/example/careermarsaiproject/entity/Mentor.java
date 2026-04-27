package com.example.careermarsaiproject.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * <p>
 * 导师表
 * </p>
 *
 * @author yeling
 * @since 2026-01-20
 */
@Data
@TableName("tb_mentor")
public class Mentor implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 导师名称
     */
    private String menName;

    /**
     * 密码，加密存储
     */
    private String password;

    /**
     * 注册手机号
     */
    private String phone;

    /**
     * 创建时间
     */
    private LocalDateTime created;

    /**
     * 密码加密的salt值
     */
    private String salt;

    /**
     * 头像
     */
    private String headImage;

    /**
     * 是否开通咨询
     */
    private Integer isConsult;

    /**
     * 咨询服务价格
     */
    private Integer consultPrice;

    /**
     * 导师级别
     */
    private Integer level;

    /**
     * 毕业院校
     */
    private String schoolName;

    /**
     * 任职企业
     */
    private String workEnterprise;

    /**
     * 导师状态
     */
    private Integer menState;

    /**
     * 领域id
     */
    private String lableIds;

    /**
     * 领域名称
     */
    private String lableNames;

    private String intro;

    private LocalDateTime createDate;

    private LocalDateTime updateDate;

    private Integer weight;

    private Integer studyCount;

    private Integer payCount;

    private Integer courseCount;

    private Integer sex;

    private String countryCode;

    private String countryNum;
}
