package com.example.careermarsaiproject.config;//package com.example.careermarsaiproject.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Slf4j
@Component
public class AiConfig {
    @Value("${spring.ai.api-key}")
    private String apiKey ;
    @Value("${spring.ai.model}")
    private String model ;
    // 增强版强制格式提示
    public static final String FORCE_PROMPT =
            "\n【输出格式强制要求】：" +
                    "\n1. 仅返回标准JSON格式内容，不添加任何解释、说明、备注、换行符" +
                    "\n2. JSON字段名必须使用双引号包裹，值为字符串的也必须用双引号" +
                    "\n3. 禁止使用单引号、中文标点、注释、多余逗号" +
                    "\n4. 严格按照指定的字段结构返回，字段名和数据类型必须完全匹配" +
                    "\n5. 即使没有匹配数据，也必须返回空数组[]或默认值，禁止返回null或空对象" +
                    "\n6. 数值类型直接返回数字，不要用引号包裹";
}
