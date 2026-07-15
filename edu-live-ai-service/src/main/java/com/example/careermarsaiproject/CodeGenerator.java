package com.example.careermarsaiproject;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import java.util.HashMap;

public class CodeGenerator {

        public static void main(String[] args) {
            String url = "jdbc:mysql://127.0.0.1:3306/edu_live?useSSL=false&useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=false";
            String username = "root";
            String password = "root";

            FastAutoGenerator.create(url, username, password)
                    .globalConfig(builder -> builder
                            .author("叶陵")
                            .outputDir(System.getProperty("user.dir") + "/edu-live-ai-service/src/main/java")
                            .disableOpenDir()
                    )
                    .packageConfig(builder -> builder
                            .parent("com.example.careermarsaiproject") // 父包名
                            .entity("entity")
                            .mapper("mapper")
                            .service("service")
                            .serviceImpl("impl")
                            .pathInfo(new HashMap<OutputFile, String>() {{
                                put(OutputFile.service, System.getProperty("user.dir") + "/edu-live-ai-service/src/main/java/com/example/careermarsaiproject/service");
                                put(OutputFile.serviceImpl, System.getProperty("user.dir") + "/edu-live-ai-service/src/main/java/com/example/careermarsaiproject/service/impl");
                                put(OutputFile.entity, System.getProperty("user.dir") + "/edu-live-ai-service/src/main/java/com/example/careermarsaiproject/entity");
                                put(OutputFile.mapper, System.getProperty("user.dir") + "/edu-live-ai-service/src/main/java/com/example/careermarsaiproject/mapper");
                                put(OutputFile.xml, System.getProperty("user.dir") + "/edu-live-ai-service/src/main/resources/mapper");
                            }})
                    )
                    .strategyConfig(builder -> builder
                                    .addInclude("tb_consultation_record") // 指定生成的表
                                    .addTablePrefix("tb_")
                                    .entityBuilder()
                                    .enableLombok()
                                    .naming(NamingStrategy.underline_to_camel)
                                    .mapperBuilder()
                                    .enableMapperAnnotation()
                                    .enableBaseResultMap()
                                    .formatMapperFileName("%sMapper")
                    )
                    .execute();
        }

}