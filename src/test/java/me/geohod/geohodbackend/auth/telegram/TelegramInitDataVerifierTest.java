package me.geohod.geohodbackend.auth.telegram;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import me.geohod.geohodbackend.configuration.properties.GeohodProperties;

class TelegramInitDataVerifierTest {

    private static final String BOT_TOKEN = "1234567:ABC-DEF1234ghIkl-zyx57W2v1u123ew11";
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private TelegramInitDataVerifier verifier;

    @BeforeEach
    void setUp() {
        var telegramBot = new GeohodProperties.TelegramBot(BOT_TOKEN, "testbot");
        var telegramInitData = new GeohodProperties.TelegramInitData(Duration.ofMinutes(5));
        var security = new GeohodProperties.Security(null, null, null, null, telegramInitData);
        var properties = new GeohodProperties(telegramBot, null, null, security);
        verifier = new TelegramInitDataVerifier(properties);
    }

    @Test
    void verifyAndExtract_validInitData_returnsVerifiedData() {
        String userJson = "{\"id\":12345,\"first_name\":\"John\",\"last_name\":\"Doe\",\"username\":\"johndoe\",\"photo_url\":\"https://example.com/photo.jpg\"}";
        String authDate = String.valueOf(Instant.now().getEpochSecond());

        String initData = buildInitData(userJson, authDate);

        VerifiedInitData result = verifier.verifyAndExtract(initData);

        assertNotNull(result);
        assertEquals("12345", result.telegramUserId());
        assertEquals("johndoe", result.username());
        assertEquals("John", result.firstName());
        assertEquals("Doe", result.lastName());
        assertEquals("https://example.com/photo.jpg", result.photoUrl());
    }

    @Test
    void verifyAndExtract_expiredAuthDate_throwsSecurityException() {
        String userJson = "{\"id\":12345,\"first_name\":\"John\",\"last_name\":\"Doe\",\"username\":\"johndoe\"}";
        String authDate = String.valueOf(Instant.now().minus(Duration.ofMinutes(10)).getEpochSecond());

        String initData = buildInitData(userJson, authDate);

        SecurityException exception = assertThrows(SecurityException.class,
                () -> verifier.verifyAndExtract(initData));
        assertEquals("Expired auth_date in initData", exception.getMessage());
    }

    @Test
    void verifyAndExtract_invalidHash_throwsSecurityException() {
        String userJson = "{\"id\":12345,\"first_name\":\"John\",\"last_name\":\"Doe\",\"username\":\"johndoe\"}";
        String authDate = String.valueOf(Instant.now().getEpochSecond());
        String encodedUser = URLEncoder.encode(userJson, StandardCharsets.UTF_8);

        String initData = "auth_date=" + authDate
                + "&user=" + encodedUser
                + "&hash=0000000000000000000000000000000000000000000000000000000000000000";

        SecurityException exception = assertThrows(SecurityException.class,
                () -> verifier.verifyAndExtract(initData));
        assertEquals("Invalid initData hash", exception.getMessage());
    }

    private String buildInitData(String userJson, String authDate) {
        String encodedUser = URLEncoder.encode(userJson, StandardCharsets.UTF_8);

        // Build the data check string the same way the verifier does:
        // parse URL-encoded params (which URL-decodes values), remove hash, sort by key, join with \n
        Map<String, String> sortedParams = new TreeMap<>();
        sortedParams.put("auth_date", authDate);
        sortedParams.put("user", userJson); // decoded value, as parseInitData URL-decodes

        String dataCheckString = sortedParams.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");

        String hash = computeHash(dataCheckString);

        return "auth_date=" + authDate
                + "&user=" + encodedUser
                + "&hash=" + hash;
    }

    private String computeHash(String dataCheckString) {
        try {
            Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
            hmac.init(new SecretKeySpec("WebAppData".getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] secretKey = hmac.doFinal(BOT_TOKEN.getBytes(StandardCharsets.UTF_8));

            Mac dataHmac = Mac.getInstance(HMAC_ALGORITHM);
            dataHmac.init(new SecretKeySpec(secretKey, HMAC_ALGORITHM));
            byte[] hashBytes = dataHmac.doFinal(dataCheckString.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
