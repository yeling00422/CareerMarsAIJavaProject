package com.example.careermarsaiproject.service.impl;

import com.example.careermarsaiproject.entity.Job;
import com.example.careermarsaiproject.mapper.JobMapper;
import com.example.careermarsaiproject.service.IJobService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 岗位数据表 服务实现类
 * </p>
 *
 * @author 叶陵
 * @since 2026-01-27
 */
@Service
public class JobServiceImpl extends ServiceImpl<JobMapper, Job> implements IJobService {

}
