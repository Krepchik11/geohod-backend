package me.geohod.geohodbackend.service;

public interface ITelegramBotService {
    void sendMessage(Long chatId, String message);
}