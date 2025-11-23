package me.geohod.geohodbackend.service.notification.processor.strategy.message;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TemplateEngine {
    
    // Pattern for variable interpolation: {{variable}} or {{variable|fallback}} or {{variable:50}}
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");
    
    // Pattern for conditional blocks: {#if variable}content{/if}
    private static final Pattern IF_PATTERN = Pattern.compile("\\{#if\\s+([^}]+)\\}([^#]*?)\\{/if\\}", Pattern.DOTALL);
    
    // Pattern for length limiters: {{variable:50}}
    private static final Pattern LENGTH_PATTERN = Pattern.compile("([^:]+):(\\d+)");
    
    public String processTemplate(String template, Map<String, Object> data, String fallbackValue) {
        if (template == null || template.trim().isEmpty()) {
            return "";
        }
        
        String processed = template;
        
        processed = processConditionals(processed, data);
        
        processed = processVariables(processed, data, fallbackValue);
        
        return processed.trim();
    }
    
    public String processTemplate(String template, Map<String, Object> data) {
        return processTemplate(template, data, "");
    }
    
    private String processConditionals(String template, Map<String, Object> data) {
        Matcher matcher = IF_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String condition = matcher.group(1).trim();
            String content = matcher.group(2);
            
            boolean conditionTrue = evaluateCondition(condition, data);
            String replacement = conditionTrue ? content : "";
            
            // Escape special regex characters in replacement
            String escapedReplacement = Matcher.quoteReplacement(replacement);
            matcher.appendReplacement(result, escapedReplacement);
        }
        matcher.appendTail(result);
        
        return result.toString();
    }

    private String processVariables(String template, Map<String, Object> data, String fallbackValue) {
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String variableExpression = matcher.group(1).trim();
            String replacement = processVariableExpression(variableExpression, data, fallbackValue);
            
            // Escape special regex characters in replacement
            String escapedReplacement = Matcher.quoteReplacement(replacement);
            matcher.appendReplacement(result, escapedReplacement);
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    private String processVariableExpression(String expression, Map<String, Object> data, String fallbackValue) {
        Matcher lengthMatcher = LENGTH_PATTERN.matcher(expression);
        if (lengthMatcher.matches()) {
            String variableName = lengthMatcher.group(1).trim();
            int maxLength = Integer.parseInt(lengthMatcher.group(2));
            String value = getVariableValue(variableName, data, fallbackValue);
            return truncateValue(value, maxLength);
        }
        
        if (expression.contains("|")) {
            String[] parts = expression.split("\\|", 2);
            String variableName = parts[0].trim();
            String explicitFallback = parts[1].trim();
            return getVariableValue(variableName, data, explicitFallback);
        }
        
        return getVariableValue(expression, data, fallbackValue);
    }
    
    private String getVariableValue(String variableName, Map<String, Object> data, String fallbackValue) {
        if (data.containsKey(variableName) && data.get(variableName) != null) {
            return String.valueOf(data.get(variableName));
        }
        return fallbackValue;
    }
    
    private String truncateValue(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
    
    private boolean evaluateCondition(String condition, Map<String, Object> data) {
        // Simple boolean evaluation
        if ("true".equalsIgnoreCase(condition) || "false".equalsIgnoreCase(condition)) {
            return Boolean.parseBoolean(condition);
        }
        
        // Variable-based evaluation
        if (data.containsKey(condition)) {
            Object value = data.get(condition);
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
            if (value instanceof String) {
                return !((String) value).isEmpty();
            }
            if (value instanceof Number) {
                return ((Number) value).doubleValue() != 0;
            }
        }
        
        return false;
    }
    
    public Map<String, Object> createDataContext(MessageTemplate template, Map<String, Object> additionalData) {
        Map<String, Object> context = new HashMap<>();
        
        if (template.variables() != null) {
            context.putAll(template.variables());
        }
        
        if (additionalData != null) {
            context.putAll(additionalData);
        }
        
        return context;
    }
}