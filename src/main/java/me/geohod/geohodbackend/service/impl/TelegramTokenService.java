package me.geohod.geohodbackend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.api.dto.TelegramInitDataDto;
import me.geohod.geohodbackend.configuration.properties.GeohodProperties;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TelegramTokenService {
    private final GeohodProperties properties;
    private static final String ALGORITHM = "HmacSHA256";
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public boolean verifyTelegramWebAppData(String tgInitData) {
        try {
            Map<String, String> initDataMap = parseInitData(tgInitData);

            String hash = initDataMap.remove("hash");
            String dataToCheck = initDataMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .reduce((a, b) -> a + "\n" + b)
                    .orElse("");

            String calculatedHash = calculateHash(dataToCheck);

            return hash.equals(calculatedHash);
        } catch (Exception e) {
            throw new SecurityException("Invalid initData", e);
        }
    }

    public TelegramInitDataDto extractUserData(String tgInitData) {
        if (!verifyTelegramWebAppData(tgInitData)) {
            throw new SecurityException("Invalid initData");
        }

        Map<String, String> initDataMap = parseInitData(tgInitData);
        String userDataJson = URLDecoder.decode(initDataMap.get("user"), StandardCharsets.UTF_8);

        try {
            return objectMapper.readValue(userDataJson, TelegramInitDataDto.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid user data", e);
        }
    }

    private Map<String, String> parseInitData(String tgInitData) {
        Map<String, String> map = new HashMap<>();
        String[] pairs = tgInitData.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                map.put(URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8));
            } else {
                // Handle malformed pairs gracefully - parameters without "="
                map.put(URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8), "");
            }
        }
        return map;
    }

    private byte[] createSecretKey() {
        String botToken = properties.telegramBot().token();
        try {
            Mac hmac = Mac.getInstance(ALGORITHM);
            hmac.init(new SecretKeySpec("WebAppData".getBytes(StandardCharsets.UTF_8), ALGORITHM));
            return hmac.doFinal(botToken.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private String calculateHash(String dataToCheck) throws Exception {
        Mac hmac = Mac.getInstance(ALGORITHM);
        hmac.init(new SecretKeySpec(createSecretKey(), ALGORITHM));
        byte[] hashBytes = hmac.doFinal(dataToCheck.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
