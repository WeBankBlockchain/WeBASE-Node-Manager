package com.webank.webase.node.mgr.base.filter;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.code.RetCode;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.HttpRequestTools;
import com.webank.webase.node.mgr.frontgroupmap.entity.FrontGroup;
import com.webank.webase.node.mgr.frontgroupmap.entity.FrontGroupMapCache;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Log4j2
@Component
@WebFilter(filterName = "frontFilter")
public class FrontFilter implements Filter {

    @Autowired
    private ConstantProperties properties;
    @Autowired
    private FrontGroupMapCache frontGroupMapCache;

    private List<String> ignorePath;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ignorePath = getIgnorePath();
        log.info("ignorePath:{}", JSON.toJSONString(ignorePath));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
        throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String uri = HttpRequestTools.getUri(httpRequest);
        if (isIgnore(uri)) {
            log.debug("FrontFilter ignore:{}", uri);
            filterChain.doFilter(request, response);
        } else {
            List<FrontGroup> list = frontGroupMapCache.getAllMap();
            if (CollectionUtils.isEmpty(list)) {
                log.warn("fail FrontFilter. not fount any front.");
                RetCode retCode = ConstantCode.FRONT_LIST_NOT_FOUNT;
                String messageStr = retCode.getCode() + "_" + retCode.getMsg();
                throw new RuntimeException(messageStr);
            }
            filterChain.doFilter(request, response);
        }
    }

    /**
     * check path is ignore.
     */
    private boolean isIgnore(String uri) {
        long count = ignorePath.stream().filter(path -> uri.startsWith(path)).count();
        return count > 0;
    }

    /**
     * get ignore path.
     */
    private List<String> getIgnorePath() {
        String ignorePathStr = properties.getIgnoreCheckFront();
        if (StringUtils.isBlank(ignorePathStr)) {
            return null;
        }
        List<String> ignorePathList = Arrays.asList(ignorePathStr.split(","));
        return ignorePathList;
    }
}
