package com.treexor.config.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(VersionFilter.class);

    private Integer lastApiVersion;

    public VersionFilter(Integer lastApiVersion) {
        this.lastApiVersion = lastApiVersion;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        String requestURI = ((HttpServletRequest) servletRequest).getRequestURI();
        if (requestURI.matches("^\\/api\\/v\\d+\\/.+")) {
            String sApiVersion = StringUtils.substringBetween(requestURI, "v", "/");
            if (sApiVersion != null) {
                try {
                    Integer apiVersion = Integer.valueOf(sApiVersion);

                    // check api version is supported
                    if (apiVersion >= 1 && apiVersion <= lastApiVersion) {

                        // proceed to controllers
                        filterChain.doFilter(servletRequest, servletResponse);
                        return;
                    }
                } catch (NumberFormatException e) {
                    log.warn("Request URI version is not an integer: %s", sApiVersion);
                }
            }
        } else {
            HttpServletResponse resp = (HttpServletResponse) servletResponse;
            resp.reset();
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        HttpServletResponse resp = (HttpServletResponse) servletResponse;
        resp.reset();
        resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        return;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
