package com.example.careermarsaiproject.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Slf4j
@Component
public class DatasourceConfig {
    @Value("${spring.datasource.username}")
    private String username ;
    @Value("${spring.datasource.password}")
    private String password ;
    @Value("${spring.datasource.url}")
    private String url ;
    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName ;
}
