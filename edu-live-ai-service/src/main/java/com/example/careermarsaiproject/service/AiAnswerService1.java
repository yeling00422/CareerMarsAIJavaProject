package com.example.careermarsaiproject.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.example.careermarsaiproject.config.AiConfig;
import com.example.careermarsaiproject.config.AiJsonCleanerConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class AiAnswerService1 {
    // 全局复用客户端（避免重复创建，性能提升巨大）
    private final Generation gen = new Generation();

    @Autowired
    private AiConfig aiConfig;
    @Autowired
    private ObjectMapper objectMapper;

    // ====================== 统一AI调用核心 ======================
    public String callWithMessage(String question,int maxRetry) throws Exception {
        String finalQuestion = question + AiConfig.FORCE_PROMPT;

        // 构建请求参数（只构建一次）
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("你是严格按照格式要求输出JSON的助手，只返回标准JSON，不添加任何额外内容")
                .build();

        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(finalQuestion)
                .build();

        GenerationParam param = GenerationParam.builder()
                .apiKey(aiConfig.getApiKey())
                .model(aiConfig.getModel())
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .temperature(0.1F)
                .build();

        long timeout = 10000; // 10秒超时（关键！不会卡死）
        for (int i = 0; i < maxRetry; i++) {
            try {
                // 带超时执行
                String result = CompletableFuture.supplyAsync(() -> {
                    try {
                        return gen.call(param).getOutput().getChoices().get(0).getMessage().getContent();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).get(timeout, TimeUnit.MILLISECONDS);
                // JSON清洗（解决99%格式错误）
                return AiJsonCleanerConfig.cleanJson(result);
            } catch (TimeoutException e) {
                log.error("AI调用超时，重试第 {} 次", i);
            } catch (Exception e) {
                log.error("AI调用失败，重试第 {} 次，错误：{}", i, e.getMessage());
            }
        }
        return null;
    }

}