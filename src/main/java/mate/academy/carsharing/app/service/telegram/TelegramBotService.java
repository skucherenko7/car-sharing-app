package mate.academy.carsharing.app.service.telegram;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mate.academy.carsharing.app.exception.TelegramApiException;
import mate.academy.carsharing.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramBotService {
    private static final String TELEGRAM_API_BASE_URL = "https://api.telegram.org/bot";

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    @Value("${telegram.bot.token}")
    private String botToken;

    private int lastUpdateId = 0;

    @Scheduled(fixedDelay = 3000)
    public void pollTelegramUpdates() {
        String url = UriComponentsBuilder
                .fromHttpUrl(TELEGRAM_API_BASE_URL + botToken + "/getUpdates")
                .queryParam("offset", lastUpdateId + 1)
                .toUriString();

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            List<Map<String, Object>> updates = (List<Map<String, Object>>) response.get("result");

            if (updates != null && !updates.isEmpty()) {
                for (Map<String, Object> update : updates) {
                    lastUpdateId = (int) update.get("update_id");
                    handleUpdate(update);
                }
            }
        } catch (Exception e) {
            log.error("Telegram polling error", e);
            throw new TelegramApiException("Failed to read Telegram response", e);
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

    public void sendGreetingMessage(String chatId) {
        String messageText = "Welcome to the Car Sharing Notification Bot!\n"
                + "You will now receive updates here.";

        String encodedText = URLEncoder.encode(messageText, StandardCharsets.UTF_8);

        String url = UriComponentsBuilder
                .fromHttpUrl(TELEGRAM_API_BASE_URL + botToken + "/sendMessage")
                .queryParam("chat_id", chatId)
                .queryParam("text", encodedText)
                .toUriString();

        try {
            restTemplate.getForObject(url, String.class);
            log.info("Greeting sent to chatId {}", chatId);
        } catch (Exception e) {
            log.error("Failed to send greeting", e);
            throw new TelegramApiException("Failed to send greeting", e);
        }
    }
}
