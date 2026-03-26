package me.geohod.geohodbackend.service.notification.processor.strategy;

import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.service.notification.NotificationChannel;

public interface NotificationStrategy {
    NotificationChannel getChannel();

    void send(Event event, String payload);
}