package me.geohod.geohodbackend.service.notification.processor.strategy.message;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MessageTemplateRegistry {
    
    private final Map<String, Map<TemplateType, MessageTemplate>> templates = new HashMap<>();
    
    public void registerTemplate(MessageTemplate template) {
        if (template == null) {
            throw new IllegalArgumentException("Template cannot be null");
        }
        
        templates.computeIfAbsent(template.id(), k -> new HashMap<>())
                .put(template.type(), template);
                
        log.debug("Registered template: {} for type: {}", template.id(), template.type());
    }
    
    public MessageTemplate getTemplate(String templateId, TemplateType templateType) {
        if (templateId == null || templateType == null) {
            return null;
        }
        
        Map<TemplateType, MessageTemplate> typeTemplates = templates.get(templateId);
        if (typeTemplates == null) {
            log.debug("No templates found for ID: {}", templateId);
            return null;
        }
        
        MessageTemplate template = typeTemplates.get(templateType);
        if (template == null) {
            log.debug("No template found for ID: {} and type: {}", templateId, templateType);
        }
        
        return template;
    }
    
    public MessageTemplate getTemplateWithFallback(String templateId, TemplateType preferredType, TemplateType fallbackType) {
        MessageTemplate template = getTemplate(templateId, preferredType);
        if (template != null) {
            return template;
        }
        
        log.debug("No template found for ID: {} and type: {}, trying fallback: {}", 
                 templateId, preferredType, fallbackType);
        return getTemplate(templateId, fallbackType);
    }
    
    public Map<TemplateType, MessageTemplate> getAllTemplates(String templateId) {
        return templates.getOrDefault(templateId, Map.of());
    }
    
    public Map<String, Map<TemplateType, MessageTemplate>> getAllTemplates() {
        return Map.copyOf(templates);
    }
    
    public void initializeDefaultTemplates() {
        registerTemplate(MessageTemplate.withFallbacks(
            "event.created",
            """
            Вы создали событие:

            [{{eventName}}]({{eventLink}})
            {{eventDate}}

            Ссылка для регистрации:
            {{registrationLink}}""",
            TemplateType.TELEGRAM,
            Map.of(),
            Map.of()
        ));
        
        registerTemplate(MessageTemplate.withFallbacks(
            "event.cancelled.organizer.notify-participants",
            """
            Вы отменили событие:
            
            [{{eventName}}]({{eventLink}})
            {{eventDate}}
            
            Уведомления об отмене события отправлены следующим участникам:
            {{participantList}}
            {{contactInfo}}""",
            TemplateType.TELEGRAM,
            Map.of(),
            Map.of("contactInfo", "Информация недоступна")
        ));

        registerTemplate(MessageTemplate.withFallbacks(
            "event.cancelled.organizer.not-notify-participants",
            """
            Вы отменили событие:
            
            [{{eventName}}]({{eventLink}})
            {{eventDate}}
            
            Уведомления об отмене события участникам не направлено""",
            TemplateType.TELEGRAM,
            Map.of(),
            Map.of("contactInfo", "Информация недоступна")
        ));

        registerTemplate(MessageTemplate.withFallbacks(
            "event.cancelled",
            """
            Событие отменено:

            [{{eventName}}]({{eventLink}})
            {{eventDate}}
            [{{contactName}}]({{contactLink}})""",
            TemplateType.TELEGRAM,
            Map.of(),
            Map.of("contactName", "Информация недоступна", "contactLink", "")
        ));

        registerTemplate(MessageTemplate.withFallbacks(
            "participant.registered",
            """
            Вы зарегистрировались на событие:

            [{{eventName}}]({{eventLink}})
            {{eventDate}}
            [{{contactName}}]({{contactLink}})""",
            TemplateType.TELEGRAM,
            Map.of(),
            Map.of("contactName", "", "contactLink", "")
        ));

        registerTemplate(MessageTemplate.withFallbacks(
            "participant.unregistered",
            """
            Вы отменили регистрацию на событие:

            [{{eventName}}]({{eventLink}})
            {{eventDate}}""",
            TemplateType.TELEGRAM,
            Map.of(),
            Map.of()
        ));

        registerTemplate(MessageTemplate.withFallbacks(
            "event.finished",
            """
            Событие завершено:

            [{{eventName}}]({{eventLink}})
            {{eventDate}}

            Ваш отзыв важен — оставьте его по [ссылке]({{reviewLink}})""",
            TemplateType.TELEGRAM,
            Map.of(),
            Map.of()
        ));

        log.info("Initialized {} default templates", templates.size());
    }
}