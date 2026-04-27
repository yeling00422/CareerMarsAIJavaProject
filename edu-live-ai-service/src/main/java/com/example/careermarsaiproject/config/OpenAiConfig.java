//package com.example.careermarsaiproject.config;//package com.example.careermarsaiproject.config;
//
//import com.theokanning.openai.service.OpenAiService;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.stereotype.Component;
//
//import java.time.Duration;
//
//@Data
//@Slf4j
//@Component
//public class OpenAiConfig {
//    @Value("${openai.api-key}")
//    private String apiKey ;
//    @Value("${openai.model}")
//    private String model ;
//    @Value("${openai.base-url}")
//    private String baseUrl;
//
//    @Bean
//    public OpenAiService openAiService() {
//        return new OpenAiService(apiKey, Duration.ofSeconds(60));
//    }
//}
