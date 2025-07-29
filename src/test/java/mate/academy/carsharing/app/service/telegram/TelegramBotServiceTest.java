package mate.academy.carsharing.app.service.telegram;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import mate.academy.carsharing.app.config.TestTelegramConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@Import(TestTelegramConfig.class)
@TestPropertySource(properties = {
        "telegram.bot.token=7438200647:AAHOHWc8Oaa4vdgEU-GHOKVC3jf4xsO_L5o",
        "spring.task.scheduling.enabled=false"
})

class TelegramBotServiceTest {

    @Autowired
    private TelegramBotService telegramBotService;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @Autowired
    private Environment env;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    @DisplayName("SendGreetingMessage: sends a welcome message without errors.")
    void sendGreetingMessage_shouldSendMessageWithoutError() {
        String chatId = "123456789";
        String token = env.getProperty("telegram.bot.token");

        mockServer.expect(requestTo(containsString("/bot/" + token + "/sendMessage")))
                .andExpect(requestTo(containsString("chat_id=" + chatId)))
                .andExpect(requestTo(containsString("text=")))
                .andRespond(withSuccess("{\"ok\":true}", MediaType.APPLICATION_JSON));

        telegramBotService.sendGreetingMessage(chatId);

        mockServer.verify();
    }

    @Test
    @DisplayName("IsTestEnvironment: returns true if skip.telegram."
            + "scheduling system property is true.")
    void isTestEnvironment_shouldReturnTrueIfSystemPropertySet() {
        System.setProperty("skip.telegram.scheduling", "true");
        boolean result = (Boolean) ReflectionTestUtils
                .invokeMethod(telegramBotService, "isTestEnvironment");
        assertThat(result).isTrue();
        System.clearProperty("skip.telegram.scheduling");
    }

    @Test
    @DisplayName("IsTestEnvironment: returns true if active profile is test.")
    void isTestEnvironment_shouldReturnTrueIfTestProfileActive() {
        boolean result = (Boolean) ReflectionTestUtils
                .invokeMethod(telegramBotService, "isTestEnvironment");
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("HandleUpdate: sends greeting on /start message.")
    void handleUpdate_shouldSendGreetingOnStart() throws Exception {
        Map<String, Object> chat = new HashMap<>();
        chat.put("id", "123456789");

        Map<String, Object> message = new HashMap<>();
        message.put("text", "/start");
        message.put("chat", chat);

        Map<String, Object> update = new HashMap<>();
        update.put("message", message);

        TelegramBotService spyService = org.mockito.Mockito.spy(telegramBotService);
        org.mockito.Mockito.doNothing().when(spyService).sendGreetingMessage("123456789");

        Method method = TelegramBotService.class.getDeclaredMethod("handleUpdate", Map.class);
        method.setAccessible(true);
        method.invoke(spyService, update);

        org.mockito.Mockito.verify(spyService).sendGreetingMessage("123456789");
    }

    @Test
    @DisplayName("HandleUpdate: ignores update without message.")
    void handleUpdate_shouldIgnoreIfNoMessage() throws Exception {
        Map<String, Object> update = new HashMap<>();

        Method method = TelegramBotService.class.getDeclaredMethod("handleUpdate", Map.class);
        method.setAccessible(true);

        assertDoesNotThrow(() -> method.invoke(telegramBotService, update));
    }

    @Test
    @DisplayName("PollTelegramUpdates: does nothing in test environment.")
    void pollTelegramUpdates_shouldSkipInTestEnv() {
        System.setProperty("skip.telegram.scheduling", "true");
        assertDoesNotThrow(() -> telegramBotService.pollTelegramUpdates());
        System.clearProperty("skip.telegram.scheduling");
    }

    @Test
    @DisplayName("PollTelegramUpdates: logs info in normal env.")
    void pollTelegramUpdates_shouldLogInfo() {
        assertDoesNotThrow(() -> telegramBotService.pollTelegramUpdates());
    }
}
