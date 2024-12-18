package me.geohod.geohodbackend.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class ExceptionHandlerAdvice {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex, WebRequest request) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("error", ex.getMessage());
        errorDetails.put("path", request.getDescription(false));

        log.error("System error occurred: {}", errorDetails, ex);

        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}