package com.webank.webase.node.mgr.config.security;

import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.annotation.SecuredAnnotationSecurityMetadataSource;
import org.springframework.security.access.expression.method.ExpressionBasedAnnotationAttributeFactory;
import org.springframework.security.access.method.MethodSecurityMetadataSource;
import org.springframework.security.access.prepost.PrePostAnnotationSecurityMetadataSource;
import org.springframework.security.access.prepost.PrePostInvocationAttributeFactory;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

/**
 * if useSecurity is false, disabled @PreAuthorize annotation
 */
@Log4j2
@ConditionalOnProperty(name = "constant.isUseSecurity", havingValue = "true")
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {

    @Autowired private ConstantProperties constants;

    protected MethodSecurityMetadataSource customMethodSecurityMetadataSource() {
        boolean enablePreAuthorize = constants.getIsUseSecurity();
        log.info("customMethodSecurityMetadataSource enablePreAuthorize:{}", enablePreAuthorize);
//        return enablePreAuthorize ? new SecuredAnnotationSecurityMetadataSource() : null;
        return enablePreAuthorize ? new PrePostAnnotationSecurityMetadataSource(
            new ExpressionBasedAnnotationAttributeFactory(getExpressionHandler())) : null;
    }

}