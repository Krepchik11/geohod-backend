package me.geohod.geohodbackend.service.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import me.geohod.geohodbackend.service.notification.processor.strategy.message.MessageTemplate;
import me.geohod.geohodbackend.service.notification.processor.strategy.message.TemplateEngine;
import me.geohod.geohodbackend.service.notification.processor.strategy.message.TemplateType;

class MessageTemplateTest {
    @Test
    void variableSubstitution() {
        TemplateEngine engine = new TemplateEngine();
        String template = "Event: {{eventName}} on {{eventDate}}";
        Map<String, Object> data = Map.of(
            "eventName", "Java Meetup",
            "eventDate", "2025-01-15"
        );
        
        String result = engine.processTemplate(template, data);
        
        assertEquals("Event: Java Meetup on 2025-01-15", result);
    }

    @Test
    void conditionalLogic() {
        TemplateEngine engine = new TemplateEngine();
        String template = "Event: {{eventName}}{#if contactInfo}\nContact: {{contactInfo}}{/if}{#if nully}Null: {{nully}}{/if}";
        Map<String, Object> data = Map.of(
            "eventName", "Tech Conference",
            "contactInfo", "John Doe @johndoe",
            "nully", ""
        );
        
        String result = engine.processTemplate(template, data);
    
        assertEquals("Event: Tech Conference\nContact: John Doe @johndoe", result);
    }

    @Test
    void variableResolution() {
        MessageTemplate template = MessageTemplate.withVariables(
            "test.template", 
            "{{eventName}} - {{authorName}}", 
            TemplateType.TELEGRAM,
            Map.of("authorName", "John Doe")
        );
        
        Map<String, Object> data = Map.of(
            "eventName", "Spring Boot Workshop"
        );
        
        String eventName = template.getVariable("eventName", data).orElse("");
        String authorName = template.getVariable("authorName", data).orElse("");
        
        assertEquals("Spring Boot Workshop", eventName);
        assertEquals("John Doe", authorName);
    }

    @Test
    void fallbackBehavior() {
        TemplateEngine engine = new TemplateEngine();
        String template = "Event: {{eventName}} by {{organizer}}";
        Map<String, Object> data = Map.of("eventName", "Docker Workshop");
        
        String result = engine.processTemplate(template, data, "Unknown Organizer");
        
        assertEquals("Event: Docker Workshop by Unknown Organizer", result);
    }
}