package com.cuestacougars.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.RelativeRedirectFilter;

/**
 * Web layer configuration.
 * Registers RelativeRedirectFilter so Spring's redirect:/... responses
 * emit a relative Location header (e.g. /customer/dashboard) instead of
 * an absolute one (http://localhost:8080/customer/dashboard).
 * This lets the browser resolve the redirect against whatever host it is
 * currently on, which is important when the app runs behind a reverse proxy.
 */
@Configuration
public class WebConfig {

    @Bean
    public FilterRegistrationBean<RelativeRedirectFilter> relativeRedirectFilter() {
        FilterRegistrationBean<RelativeRedirectFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RelativeRedirectFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }
}
