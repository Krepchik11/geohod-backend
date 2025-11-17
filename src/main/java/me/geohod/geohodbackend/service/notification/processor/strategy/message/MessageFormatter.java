package me.geohod.geohodbackend.service.notification.processor.strategy.message;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import me.geohod.geohodbackend.configuration.properties.GeohodProperties;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.User;

@Component
@Slf4j
public class MessageFormatter {
    
    private final TemplateEngine templateEngine;
    private final MessageTemplateRegistry templateRegistry;
    
    public MessageFormatter(TemplateEngine templateEngine, MessageTemplateRegistry templateRegistry, GeohodProperties geohodProperties) {
        this.templateEngine = templateEngine;
        this.templateRegistry = templateRegistry;
        this.botName = geohodProperties.telegramBot().username();
        this.eventLinkTemplate = geohodProperties.linkTemplates().eventRegistrationLink();
    }

    private final String botName;
    private final String eventLinkTemplate;
    
    public String formatMessage(String templateId, Event event, User author, Map<String, Object> variables) {
        Map<String, Object> context = new HashMap<>(variables);
        context.put("eventName", event.getName());
        context.put("eventLink", formatEventLink(event));
        context.put("contactInfo", formatContactInfo(author));
        context.put("botName", botName);
        return templateEngine.processTemplate(templateId, context);
    }
    
    public String formatMessageFromTemplate(String templateId, TemplateType templateType, 
                                          Event event, User author, Map<String, Object> variables) {
        
        MessageTemplate template = templateRegistry.getTemplate(templateId, templateType);
        if (template == null) {
            log.error("Template not found: {} for type: {}", templateId, templateType);
            return formatFallbackMessage(templateId, event);
        }
        
        return formatWithTemplate(template, event, author, variables);
    }
    
    private String formatWithTemplate(MessageTemplate template, Event event, User author, Map<String, Object> variables) {
        Map<String, Object> dataContext = buildDataContext(event, author, variables);
        
        String formattedMessage = templateEngine.processTemplate(template.template(), dataContext);
        
        return applyChannelFormatting(formattedMessage, template.type());
    }
    
    private Map<String, Object> buildDataContext(Event event, User author, Map<String, Object> variables) {
        Map<String, Object> data = new HashMap<>();
        
        data.put("eventName", event.getName());
        data.put("eventDate", formatEventDate(event.getDate()));
        data.put("eventId", event.getId().toString());
        
        data.put("authorFirstName", author.getFirstName());
        data.put("authorLastName", author.getLastName());
        data.put("authorTgUsername", author.getTgUsername());
        data.put("authorFullName", getAuthorFullName(author));
        
        String contactInfo = formatContactInfo(author);
        if (contactInfo != null && !contactInfo.isEmpty()) {
            data.put("contactInfo", contactInfo);
        }
        
        if (variables != null) {
            data.putAll(variables);
        }
        
        data.put("registrationLink", buildRegistrationLink(event));
        data.put("reviewLink", buildReviewLink(event));
        
        return data;
    }
    
    private String applyChannelFormatting(String message, TemplateType templateType) {
        return switch (templateType) {
            case TELEGRAM -> applyTelegramFormatting(message);
            case IN_APP -> applyInAppFormatting(message);
        };
    }
    
    private String applyTelegramFormatting(String message) {
        String escaped = message
            .replace("*", "\\*")
            .replace("_", "\\_")
            .replace("[", "\\[")
            .replace("]", "\\]")
            .replace("(", "\\(")
            .replace(")", "\\)")
            .replace("~", "\\~")
            .replace("`", "\\`")
            .replace(">", "\\>")
            .replace("#", "\\#")
            .replace("+", "\\+")
            .replace("-", "\\-")
            .replace("=", "\\=")
            .replace("|", "\\|")
            .replace("{", "\\{")
            .replace("}", "\\}")
            .replace(".", "\\.")
            .replace("!", "\\!");
        
        if (escaped.length() > 4096) {
            log.warn("Telegram message too long ({} chars), truncating to 4096", escaped.length());
            escaped = escaped.substring(0, 4093) + "...";
        }
        
        return escaped;
    }
    
    private String applyInAppFormatting(String message) {
        return message
            .replace("**", "")
            .replace("*", "")
            .replace("_", "")
            .replace("`", "")
            .replace("[", "")
            .replace("]", "")
            .replace("(", "")
            .replace(")", "");
    }
    
    private String formatFallbackMessage(String templateId, Event event) {
        log.warn("Using fallback message for template: {}", templateId);
        return String.format("Event notification (%s): %s on %s", 
            templateId, 
            event.getName(), 
            formatEventDate(event.getDate()));
    }
    
    private String buildRegistrationLink(Event event) {
        if (eventLinkTemplate != null && botName != null) {
            return eventLinkTemplate
                .replace("{botName}", botName)
                .replace("{eventId}", event.getId().toString());
        }
        return "";
    }
    
    private String buildReviewLink(Event event) {
        return buildRegistrationLink(event);
    }
    
    private String formatEventDate(java.time.Instant eventDate) {
        return java.time.LocalDate.ofInstant(eventDate, java.time.ZoneId.systemDefault()).toString();
    }

    private String formatContactInfo(User author) {
        String fullName = getAuthorFullName(author);
        String tgUsername = author.getTgUsername();

        if (fullName.isEmpty() && (tgUsername == null || tgUsername.trim().isEmpty())) {
            return "";
        }

        if (tgUsername == null || tgUsername.trim().isEmpty()) {
            return String.format("Организатор: %s", fullName);
        }

        if (fullName.isEmpty()) {
            return String.format("Организатор: @%s", tgUsername);
        }

        return String.format("Организатор: %s @%s", fullName, tgUsername);
    }

    private String formatEventLink(Event event) {
        return buildRegistrationLink(event);
    }

    private String getAuthorFullName(User author) {
        return java.util.stream.Stream.of(author.getFirstName(), author.getLastName())
            .filter(name -> name != null && !name.trim().isEmpty())
            .collect(java.util.stream.Collectors.joining(" "));
    }
}

