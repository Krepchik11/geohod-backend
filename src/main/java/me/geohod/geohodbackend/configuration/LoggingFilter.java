package me.geohod.geohodbackend.configuration;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LoggingFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest httpRequest) || !(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(httpResponse);

        try {
            chain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            logRequest(wrappedRequest);
            logResponse(wrappedResponse);

            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        String requestBody = new String(request.getContentAsByteArray(), StandardCharsets.UTF_8);
        logger.info("Request: method={}, uri={}, headers={}, body={}",
                request.getMethod(),
                request.getRequestURI(),
                getHeaders(request),
                requestBody);
    }

    private void logResponse(ContentCachingResponseWrapper response) {
        String responseBody = new String(response.getContentAsByteArray(), StandardCharsets.UTF_8);
        logger.info("Response: status={}, headers={}, body={}",
                response.getStatus(),
                response.getHeaderNames(),
                responseBody);
    }

    private String getHeaders(HttpServletRequest request) {
        StringBuilder headers = new StringBuilder();
        request.getHeaderNames().asIterator().forEachRemaining(headerName ->
                headers.append(headerName).append("=").append(request.getHeader(headerName)).append(", "));
        return headers.toString();
    }
}
