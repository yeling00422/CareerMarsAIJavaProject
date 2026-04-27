
package com.example.careermarsaiproject.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.pattern.PathPatternParser;
@Slf4j
@Configuration
public class GlobalCorsConfig implements WebMvcConfigurer {
//    @Value("${cors.switch:false}")
//    private boolean corSwitch;

//    @Bean
//    public CorsWebFilter corsFilter() {
//        log.info("initori 跨域");
//        CorsConfiguration config = new CorsConfiguration();
//        // 允许cookies跨域
//        config.setAllowCredentials(true);
//        // #允许向该服务器提交请求的URI，*表示全部允许，在SpringMVC中，如果设成*，会自动转成当前请求头中的Origin
////        if(corSwitch){
//            config.addAllowedOrigin("http://platform.careermars.cn");
//            config.addAllowedOrigin("http://front.careermars.cn");
//            config.addAllowedOrigin("https://front.careermars.cn");
//            config.addAllowedOrigin("http://front.careermars.cn:5174");
//            config.addAllowedOrigin("http://dw.careermars.cn:8000");
//            config.addAllowedOrigin("http://dw.careermars.cn");
//            config.addAllowedOrigin("http://localhost:5174");
//            config.addAllowedOrigin("http://dw.careermars.cn:5174");
//            config.addAllowedOrigin("http://manager.careermars.cn");
//            config.addAllowedOrigin("https://manager.careermars.cn");
//            config.addAllowedOrigin("http://manager.careermars.cn");
//            config.addAllowedOrigin("https://door.careermars.cn");
//            config.addAllowedOrigin("https://www.careermars.cn");
//            config.addAllowedOrigin("http://124.222.187.150");
//            config.addAllowedOrigin("https://124.222.187.150");
//            config.addAllowedOrigin("http://www.careermars-test.cn");
//            config.addAllowedOrigin("https://www.careermars-test.cn");
//            config.addAllowedOrigin("http://localhost:8000"); // 新增这行，允许前端本地地址
////        }else {
////            config.addAllowedOrigin("*");
////        }
//
//
////        allowCredentials
//        config.setAllowCredentials(true);
//        // #允许访问的头信息,*表示全部
//        config.addAllowedHeader("*");
//        // 预检请求的缓存时间（秒），即在这个时间段里，对于相同的跨域请求不会再预检了
//        config.setMaxAge(18000L);
//        //允许的方法 可设置* 即允许全部http请求方法类型
//        config.addAllowedMethod("OPTIONS");
//        config.addAllowedMethod("HEAD");
//        config.addAllowedMethod("GET");
//        config.addAllowedMethod("PUT");
//        config.addAllowedMethod("POST");
//        config.addAllowedMethod("DELETE");
//        config.addAllowedMethod("PATCH");
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
//        source.registerCorsConfiguration("/**", config);
//        return new CorsWebFilter(source);
//    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // 或指定具体域名如 "http://localhost:8080"
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
