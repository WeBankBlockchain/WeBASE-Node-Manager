package com.webank.webase.node.mgr.base.config;

import com.webank.webase.node.mgr.base.filter.AppIntegrationFilter;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * web configuration.
 *
 */
@Data
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Value("${server.port}")
    private int port;

    @Autowired
    private AppIntegrationFilter appIntegrationFilter;

    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(appIntegrationFilter).addPathPatterns("/api/**");// 自定义拦截的url路径
    }
}
