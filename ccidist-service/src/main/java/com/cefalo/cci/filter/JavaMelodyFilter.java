package com.cefalo.cci.filter;

import static com.cefalo.cci.config.ApplicationConfiguration.JAVA_MELODY_SYSTEM_PROP;

import net.bull.javamelody.MonitoringFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class JavaMelodyFilter extends MonitoringFilter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        Boolean monitoringEnabled = Boolean.valueOf(System.getProperty(JAVA_MELODY_SYSTEM_PROP, "true"));

        if (!monitoringEnabled) {
            chain.doFilter(request, response);
            return;
        } else {
            super.doFilter(request, response, chain);
        }
    }
}
