package carsharing.app.service.telegram;

import carsharing.app.exception.TelegramApiException;
import java.util.Arrays;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramBotService {
    private final RestTemplate restTemplate;
    private final Environment env;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.api.base-url:https://api.telegram.org/bot}")
    private String telegramApiBaseUrl;

    private volatile long lastUpdateId = 0;

    @Scheduled(fixedDelay = 5000)
    public void pollTelegramUpdates() {
        if (isTestEnvironment()) {
            return;
        }
        log.info("Polling Telegram for updates...");
    }

    private boolean isTestEnvironment() {
        if ("true".equalsIgnoreCase(System.getProperty("skip.telegram.scheduling"))) {
            return true;
        }
        return Arrays.asList(env.getActiveProfiles()).contains("test");
    }

    public void sendGreetingMessage(String chatId) {
        String messageText = "Welcome to the Car Sharing Notification"
                + " Bot!\nYou will now receive updates here.";

        String url = UriComponentsBuilder
                .fromHttpUrl(telegramApiBaseUrl)
                .pathSegment(botToken, "sendMessage")
                .queryParam("chat_id", chatId)
                .queryParam("text", messageText)
                .toUriString();

        try {
            restTemplate.getForObject(url, String.class);
            log.info("Greeting sent to chatId {}", chatId);
        } catch (Exception e) {
            log.error("Failed to send greeting", e);
            throw new TelegramApiException("Failed to send greeting", e);
        }
    }

    private void handleUpdate(Map<String, Object> update) {
        Map<String, Object> message = (Map<String, Object>) update.get("message");
        if (message == null) {
            return;
        }

        String text = (String) message.get("text");
        Map<String, Object> chat = (Map<String, Object>) message.get("chat");
        String chatId = String.valueOf(chat.get("id"));

        if ("/start".equalsIgnoreCase(text)) {
            sendGreetingMessage(chatId);
        }
    }
}
