package me.geohod.geohodbackend.service.notification.processor.strategy.message;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.User;

@Component
@Slf4j
public class MessageFormatter {

    private final TemplateEngine templateEngine;
    private final MessageTemplateRegistry templateRegistry;
    private final TelegramMarkdownV2Formatter telegramMarkdownV2Formatter;

    public MessageFormatter(TemplateEngine templateEngine, MessageTemplateRegistry templateRegistry,
            TelegramMarkdownV2Formatter telegramMarkdownV2Formatter) {
        this.templateEngine = templateEngine;
        this.templateRegistry = templateRegistry;
        this.telegramMarkdownV2Formatter = telegramMarkdownV2Formatter;
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

    private String formatWithTemplate(MessageTemplate template, Event event, User author,
            Map<String, Object> variables) {
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

        data.put("contactName", getAuthorFullName(author));
        data.put("contactLink", buildContactLink(author));

        if (variables != null) {
            data.putAll(variables);
        }

        return data;
    }

    private String applyChannelFormatting(String message, TemplateType templateType) {
        return switch (templateType) {
            case TELEGRAM -> applyTelegramFormatting(message);
            case IN_APP -> applyInAppFormatting(message);
        };
    }

    private String applyTelegramFormatting(String message) {
        if (message == null)
            return "";
        return telegramMarkdownV2Formatter.format(message);
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

    private String getAuthorFullName(User author) {
        return java.util.stream.Stream.of(author.getFirstName(), author.getLastName())
                .filter(name -> name != null && !name.trim().isEmpty())
                .collect(java.util.stream.Collectors.joining(" "));
    }

    private String buildContactLink(User author) {
        if (author.getTgUsername() != null && !author.getTgUsername().trim().isEmpty()) {
            return "https://t.me/" + author.getTgUsername();
        }
        return "";
    }
}
