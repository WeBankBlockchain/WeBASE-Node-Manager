package com.webank.webase.node.mgr;

import lombok.Getter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author zhangyang
 * @version 1.0
 * @project WeBASE-Node-Manager-3
 * @description
 * @date 2023/12/11 09:45:30
 */

@Component
public class SpringBeanUtils implements ApplicationContextAware {
    @Getter
    private static ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringBeanUtils.applicationContext = applicationContext;
    }

}