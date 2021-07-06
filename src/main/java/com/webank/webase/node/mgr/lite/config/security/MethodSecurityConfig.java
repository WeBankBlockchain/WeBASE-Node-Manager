package com.webank.webase.node.mgr.lite.config.security;

import com.webank.webase.node.mgr.lite.config.properties.ConstantProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.annotation.SecuredAnnotationSecurityMetadataSource;
import org.springframework.security.access.method.MethodSecurityMetadataSource;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

/**
 * if useSecurity is false, disabled @PreAuthorize annotation
 * @related: SecurityConfig-configure(HttpRequest), also disabled web.intercept
 */
@Log4j2
@EnableGlobalMethodSecurity(securedEnabled = true)
@Configuration
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {

    @Autowired private ConstantProperties constants;

    protected MethodSecurityMetadataSource customMethodSecurityMetadataSource() {
        boolean enablePreAuthorize = constants.getIsUseSecurity();
        log.info("customMethodSecurityMetadataSource enablePreAuthorize:{}", enablePreAuthorize);
        return enablePreAuthorize ? new SecuredAnnotationSecurityMetadataSource() : null;
    }

}