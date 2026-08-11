package com.example.careermarsaiproject.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tencent.ocr")
public class TencentOcrProp {
    private String secretId;
    private String secretKey;
    private String region;
}