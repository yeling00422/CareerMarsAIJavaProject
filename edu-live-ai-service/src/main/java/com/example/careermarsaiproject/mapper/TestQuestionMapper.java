package com.example.careermarsaiproject.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.careermarsaiproject.entity.TestQuestion;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 面试题数据表 Mapper 接口
 * </p>
 *
 * @author 叶陵
 * @since 2026-03-10
 */
@Mapper
public interface TestQuestionMapper extends BaseMapper<TestQuestion> {

}
