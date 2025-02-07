package me.geohod.geohodbackend.service.notification;

import java.util.UUID;

public record NotificationParams(
        UUID eventId,
        String donationInfo,
        String botName,
        String linkTemplate
) {

    public static NotificationParams eventCreatedParams(UUID eventId, String botName, String linkTemplate) {
        return new NotificationParams(eventId, null, botName, linkTemplate);
    }

    public static NotificationParams eventFinishedParams(String donationInfo) {
        return new NotificationParams(null, donationInfo, null, null);
    }

    public static NotificationParams empty() {
        return new NotificationParams(null, null, null, null);
    }
}