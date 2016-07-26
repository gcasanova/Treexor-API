package com.treexor.config.filters;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.redis.core.StringRedisTemplate;

public class AuthenticationFilter implements Filter {

    public static final String PRROFILE_PREFIX = "profile#";

    private StringRedisTemplate template;

    public AuthenticationFilter(StringRedisTemplate template) {
        this.template = template;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        // allow unauthenticated access to auth register / login endpoints
        String path = ((HttpServletRequest) servletRequest).getRequestURI();
        if (path.matches("\\/api\\/v\\d+\\/auth\\/register") || path.matches("\\/api\\/v\\d+\\/auth\\/login")) {
            filterChain.doFilter(servletRequest, servletResponse); // Just continue chain.
            return;
        }

        String id = ((HttpServletRequest) servletRequest).getHeader("Id");
        String token = ((HttpServletRequest) servletRequest).getHeader("Token");

        if (id != null && token != null) {

            // check token is valid
            String storedToken = template.opsForValue().get(PRROFILE_PREFIX + "token#" + id);
            if (storedToken != null && storedToken.equals(token)) {

                // token is valid, reset expiration
                template.expire(PRROFILE_PREFIX + "token#" + id, 30, TimeUnit.MINUTES);

                // proceed to controllers
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
        }

        HttpServletResponse resp = (HttpServletResponse) servletResponse;
        resp.reset();
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
