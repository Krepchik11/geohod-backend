package me.geohod.geohodbackend.service.notification;

import java.util.UUID;

public record NotificationParams(
        UUID eventId,
        String donationInfo,
        String botName,
        String linkTemplate,
        boolean sendPollLink
) {

    public static NotificationParams eventCreatedParams(UUID eventId, String botName, String linkTemplate) {
        return new NotificationParams(eventId, null, botName, linkTemplate, false);
    }

    public static NotificationParams eventFinishedParams(String donationInfo) {
        return new NotificationParams(null, donationInfo, null, null, false);
    }

    public static NotificationParams eventFinishedParams(String donationInfo, boolean sendPollLink, UUID eventId, String botName, String linkTemplate) {
        return new NotificationParams(eventId, donationInfo, botName, linkTemplate, sendPollLink);
    }

    public static NotificationParams empty() {
        return new NotificationParams(null, null, null, null, false);
    }
}