package me.geohod.geohodbackend.auth.telegram;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.configuration.properties.GeohodProperties;

@Component
@RequiredArgsConstructor
public class TelegramInitDataVerifier {

    private final GeohodProperties properties;

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public VerifiedInitData verifyAndExtract(String initData) {
        Map<String, String> params = parseInitData(initData);

        String hash = params.remove("hash");
        if (hash == null) {
            throw new SecurityException("Missing hash in initData");
        }

        if (hash.length() != 64) {
            throw new SecurityException("Invalid hash format in initData");
        }

        String dataCheckString = buildDataCheckString(params);

        String calculatedHash = calculateHash(dataCheckString);

        if (!MessageDigest.isEqual(
                hash.getBytes(StandardCharsets.UTF_8),
                calculatedHash.getBytes(StandardCharsets.UTF_8))) {
            throw new SecurityException("Invalid initData hash");
        }

        String authDateStr = params.get("auth_date");
        if (authDateStr == null) {
            throw new SecurityException("Missing auth_date in initData");
        }

        long authDateEpoch = Long.parseLong(authDateStr);
        Instant authDate = Instant.ofEpochSecond(authDateEpoch);
        Instant cutoff = Instant.now().minus(properties.security().telegramInitData().maxAge());

        if (authDate.isBefore(cutoff)) {
            throw new SecurityException("Expired auth_date in initData");
        }

        return extractUserData(params);
    }

    private String buildDataCheckString(Map<String, String> data) {
        return data.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
    }

    private Map<String, String> parseInitData(String initData) {
        Map<String, String> map = new HashMap<>();
        String[] pairs = initData.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                map.put(URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8));
            } else {
                map.put(URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8), "");
            }
        }
        return map;
    }

    private String calculateHash(String dataCheckString) {
        try {
            byte[] secretKey = createSecretKey();
            Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
            hmac.init(new SecretKeySpec(secretKey, HMAC_ALGORITHM));
            byte[] hashBytes = hmac.doFinal(dataCheckString.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new SecurityException("Failed to calculate HMAC hash", e);
        }
    }

    private byte[] createSecretKey() {
        try {
            Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
            hmac.init(new SecretKeySpec("WebAppData".getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return hmac.doFinal(properties.telegramBot().token().getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new SecurityException("Failed to create secret key", e);
        }
    }

    private VerifiedInitData extractUserData(Map<String, String> params) {
        String userJson = params.get("user");
        if (userJson == null) {
            throw new SecurityException("Missing user data in initData");
        }

        try {
            TelegramUser user = OBJECT_MAPPER.readValue(userJson, TelegramUser.class);
            return new VerifiedInitData(
                    String.valueOf(user.id()),
                    user.username(),
                    user.firstName(),
                    user.lastName(),
                    user.photoUrl()
            );
        } catch (JsonProcessingException e) {
            throw new SecurityException("Invalid user data in initData", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private record TelegramUser(
            long id,
            String firstName,
            String lastName,
            String username,
            String photoUrl
    ) {
    }
}
