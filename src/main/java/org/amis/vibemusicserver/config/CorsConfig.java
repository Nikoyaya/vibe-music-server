package org.amis.vibemusicserver.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author : KwokChichung
 * @description : 跨域资源共享(CORS)配置类
 * 配置全局CORS策略，允许前端应用跨域访问后端API接口
 * @createDate : 2026/1/6 13:33
 */
@Configuration
public class CorsConfig {
    /**
     * 配置CORS跨域设置
     * @return WebMvcConfigurer实例，包含CORS配置
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            /**
             * 添加CORS映射配置
             * @param registry CORS注册器
             */
            @Override
            public void addCorsMappings(@NotNull CorsRegistry registry) {
                registry.addMapping("/**") // 允许所有路径
                        .allowedOriginPatterns("*") // 允许所有来源（推荐使用模式匹配）
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // 允许的HTTP方法，包含OPTIONS用于预检请求
                        .allowedHeaders("*") // 允许所有请求头
                        .exposedHeaders("Authorization") // 允许前端获取 Authorization 头（用于JWT令牌）
                        .allowCredentials(true); // 允许携带凭证（Cookie、Authorization头等）
            }
        };
    }
}

