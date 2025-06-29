package me.geohod.geohodbackend.service.notification;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@RequiredArgsConstructor
public enum NotificationType {
    EVENT_CANCELLED {
        @Override
        public String formatMessage(EventContext context, NotificationParams params) {
            String baseMessage = String.format("Организатор отменил мероприятие %s (%s)",
                    context.event().getName(),
                    formatEventDate(context.event().getDate()));

            return context.getContactInfo()
                    .map(contactInfo -> baseMessage + "\n" + contactInfo)
                    .orElse(baseMessage);
        }
    },
    PARTICIPANT_REGISTERED {
        @Override
        public String formatMessage(EventContext context, NotificationParams params) {
            return String.format("""
                            Вы зарегистрировались на мероприятие %s (%s)
                            %s
                            """,
                    context.event().getName(),
                    formatEventDate(context.event().getDate()),
                    context.getContactInfo().orElse(""));
        }
    },
    PARTICIPANT_UNREGISTERED {
        @Override
        public String formatMessage(EventContext context, NotificationParams params) {
            return String.format("""
                            Вы отменили регистрацию на мероприятие %s (%s)
                            """,
                    context.event().getName(),
                    formatEventDate(context.event().getDate()));
        }
    },
    EVENT_CREATED {
        @Override
        public String formatMessage(EventContext context, NotificationParams params) {
            String linkTemplate = params.linkTemplate();
            String botName = params.botName();
            String eventId = params.eventId().toString();
            String registrationLink = linkTemplate
                    .replace("{botName}", botName)
                    .replace("{eventId}", eventId);

            return String.format("""
                            Вы создали мероприятие:
                            
                            %s
                            %s
                            %s
                            
                            Ссылка на регистрацию [ЗДЕСЬ](%s)""",
                    context.event().getName(),
                    formatEventDate(context.event().getDate()),
                    context.getContactInfo().orElse(""),
                    registrationLink);
        }
    },
    EVENT_FINISHED {
        @Override
        public String formatMessage(EventContext context, NotificationParams params) {
            String baseMessage = String.format("""
                            Мероприятие %s (%s) завершено.
                            %s""",
                    context.event().getName(),
                    formatEventDate(context.event().getDate()),
                    context.getContactInfo().orElse(""));

            String donationInfo = params.donationInfo();
            if (!StringUtils.isBlank(donationInfo)) {
                baseMessage += String.format("""
                        
                        
                        Средний размер доната: %s""", donationInfo);
            }
            return baseMessage;
        }
    };

    public abstract String formatMessage(EventContext context, NotificationParams params); // Abstract formatMessage

    private static String formatEventDate(Instant eventDate) {
        return LocalDate.ofInstant(eventDate, ZoneId.systemDefault()).toString();
    }
}