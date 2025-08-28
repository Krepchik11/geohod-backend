package me.geohod.geohodbackend.configuration;

import java.io.IOException;

import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Filter for logging HTTP requests and responses with timing information.
 * Helps identify performance bottlenecks and monitor API usage patterns.
 */
@Component
@Slf4j
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        long startTime = System.currentTimeMillis();
        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        String queryString = httpRequest.getQueryString();
        String fullUri = queryString != null ? uri + "?" + queryString : uri;

        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = httpResponse.getStatus();

            // Log request details with timing
            if (log.isInfoEnabled()) {
                log.info("HTTP {} {} -> {} ({}ms)",
                        method, fullUri, status, duration);
            }

            // Log slow requests (>100ms) as warnings
            if (duration > 100 && log.isWarnEnabled()) {
                log.warn("SLOW REQUEST: {} {} took {}ms",
                        method, fullUri, duration);
            }

            // Log errors (5xx) as errors
            if (status >= 500 && log.isErrorEnabled()) {
                log.error("SERVER ERROR: {} {} returned {} in {}ms",
                        method, fullUri, status, duration);
            }
        }
    }
}