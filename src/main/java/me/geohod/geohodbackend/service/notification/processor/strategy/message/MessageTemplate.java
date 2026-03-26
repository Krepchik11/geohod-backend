package me.geohod.geohodbackend.service.notification.processor.strategy.message;

import java.util.Map;
import java.util.Optional;

public record MessageTemplate(
        String id,
        String template,
        TemplateType type,
        Map<String, Object> variables,
        Map<String, String> fallbacks
) {
    
    public MessageTemplate {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Template ID cannot be null or empty");
        }
        if (template == null || template.trim().isEmpty()) {
            throw new IllegalArgumentException("Template cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Template type cannot be null");
        }
    }
    
    public static MessageTemplate of(String id, String template, TemplateType type) {
        return new MessageTemplate(id, template, type, Map.of(), Map.of());
    }
    
    public static MessageTemplate withVariables(String id, String template, TemplateType type, Map<String, Object> variables) {
        return new MessageTemplate(id, template, type, variables != null ? variables : Map.of(), Map.of());
    }
    
    public static MessageTemplate withFallbacks(String id, String template, TemplateType type, 
                                              Map<String, Object> variables, Map<String, String> fallbacks) {
        return new MessageTemplate(id, template, type, 
                                 variables != null ? variables : Map.of(), 
                                 fallbacks != null ? fallbacks : Map.of());
    }

    public Optional<String> getVariable(String key, Map<String, Object> data) {
        if (variables.containsKey(key)) {
            return Optional.of(String.valueOf(variables.get(key)));
        }
        
        if (data.containsKey(key)) {
            return Optional.of(String.valueOf(data.get(key)));
        }
        
        if (fallbacks.containsKey(key)) {
            return Optional.of(fallbacks.get(key));
        }
        
        return Optional.empty();
    }
    
    public Optional<String> getRawVariable(String key, Map<String, Object> data) {
        if (variables.containsKey(key)) {
            return Optional.of(String.valueOf(variables.get(key)));
        }
        
        if (data.containsKey(key)) {
            return Optional.of(String.valueOf(data.get(key)));
        }
        
        return Optional.empty();
    }

    public boolean supportsType(TemplateType type) {
        return this.type == type;
    }
}