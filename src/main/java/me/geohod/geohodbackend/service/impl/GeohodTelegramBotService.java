package me.geohod.geohodbackend.service.impl;

import lombok.extern.slf4j.Slf4j;
import me.geohod.geohodbackend.configuration.properties.GeohodProperties;
import me.geohod.geohodbackend.exception.TelegramNotificationException;
import me.geohod.geohodbackend.service.ITelegramBotService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@Slf4j
public class GeohodTelegramBotService implements ITelegramBotService {
    private final TelegramBotService telegramBot;

    public GeohodTelegramBotService(GeohodProperties properties) {
        this.telegramBot = new TelegramBotService(
                properties.telegramBot().username(),
                properties.telegramBot().token()
        );
    }

    @Override
    public void sendMessage(Long chatId, String message) {
        try {
            SendMessage request = new SendMessage(chatId.toString(), message);
            request.enableMarkdownV2(true);
            telegramBot.execute(request);
        } catch (TelegramApiException e) {
            throw new TelegramNotificationException("Error sending Telegram message", e);
        }
    }
}