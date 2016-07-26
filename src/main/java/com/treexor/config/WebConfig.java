package com.treexor.config;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.RequestContextFilter;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.treexor.config.filters.AuthenticationFilter;
import com.treexor.config.filters.VersionFilter;

@Configuration
public class WebConfig extends WebMvcAutoConfiguration {

    @Value("${api.version}")
    private Integer lastApiVersion;

    @Autowired
    private StringRedisTemplate template;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules().setVisibility(PropertyAccessor.ALL,
                Visibility.NONE).setVisibility(PropertyAccessor.FIELD, Visibility.ANY).configure(
                        SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).configure(
                                DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false).configure(
                                        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                                        false).setSerializationInclusion(Include.NON_NULL).setSerializationInclusion(
                                                Include.NON_EMPTY);
    }

    @Bean
    public Filter characterEncodingFilter() {
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");
        characterEncodingFilter.setForceEncoding(true);
        return characterEncodingFilter;
    }

    @Bean
    public FilterRegistrationBean contextFilterRegistrationBean() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        RequestContextFilter contextFilter = new RequestContextFilter();
        registrationBean.setFilter(contextFilter);
        registrationBean.setOrder(1);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean apiVersionFilterRegistrationBean() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        VersionFilter versionFilter = new VersionFilter(lastApiVersion);
        registrationBean.setFilter(versionFilter);
        registrationBean.setOrder(2);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean authenticatedUserFilterRegistrationBean() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        AuthenticationFilter authFilter = new AuthenticationFilter(template);
        registrationBean.setFilter(authFilter);
        registrationBean.setOrder(3);
        return registrationBean;
    }
}
