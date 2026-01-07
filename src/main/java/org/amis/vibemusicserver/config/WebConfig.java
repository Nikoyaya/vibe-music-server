package org.amis.vibemusicserver.config;

import org.amis.vibemusicserver.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.amis.vibemusicserver.config.InterceptorExcludePathConfig.getExcludePaths;

/**
 * @author : KwokChichung
 * @description : Web配置类，用于添加拦截器
 * @createDate : 2026/1/7 6:02
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 使用统一的排除路径配置
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns(getExcludePaths().toArray(new String[0]));
    }
}

