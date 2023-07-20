package com.webank.webase.node.mgr.config;

import com.webank.webase.node.mgr.base.annotation.CurrentAccountMethodArgumentResolver;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.config.security.filter.AccountFilter;
import com.webank.webase.node.mgr.config.security.filter.AppIntegrationFilter;
import com.webank.webase.node.mgr.tools.JsonTools;
import java.util.List;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * web configuration.
 *
 */
@Log4j2
@Data
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${server.port}")
    private int port;

    @Autowired
    private AppIntegrationFilter appIntegrationFilter;
    @Autowired
    private ConstantProperties constants;
    @Autowired
    private AccountFilter accountFilter;

    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(appIntegrationFilter).addPathPatterns("/api/**");// 自定义拦截的url路径
        log.info("addInterceptors for /api/**");
        registry.addInterceptor(accountFilter).addPathPatterns("/**")
                .excludePathPatterns(constants.getPermitUrlArray());
        log.info("addInterceptors for {}", JsonTools.toJSONString(constants.getPermitUrlArray()));
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(currentAccountMethodArgumentResolver());
    }

    @Bean
    public CurrentAccountMethodArgumentResolver currentAccountMethodArgumentResolver() {
        return new CurrentAccountMethodArgumentResolver();
    }
}
