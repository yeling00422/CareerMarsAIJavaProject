package com.example.careermarsaiproject.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 学生咨询记录
 * </p>
 *
 * @author 叶陵
 * @since 2026-07-21
 */
@Getter
@Setter
@TableName("tb_consultation_record")
public class ConsultationRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 学生id
     */
    private String studentId;

    /**
     * 导师id
     */
    private String mentorId;

    /**
     * 点击咨询次数
     */
    private Integer totalCount;

    /**
     * 处理状态 0-待处理 1-已处理 2-处理中
     */
    private Integer handelStatus;

    /**
     * 处理人id
     */
    private String handelId;

    /**
     * 处理详情
     */
    private String handelDesc;

    /**
     * 处理时间
     */
    private LocalDateTime handelTime;

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
