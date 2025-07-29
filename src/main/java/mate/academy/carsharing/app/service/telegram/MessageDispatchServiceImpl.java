package mate.academy.carsharing.app.service.telegram;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.app.exception.MessageDispatchException;
import mate.academy.carsharing.app.model.Payment;
import mate.academy.carsharing.app.model.Rental;
import mate.academy.carsharing.app.model.User;
import mate.academy.carsharing.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class MessageDispatchServiceImpl implements MessageDispatchService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Value("${telegram.api.base-url:https://api.telegram.org}")
    private String telegramApiBaseUrl;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Override
    public void sendMessage(Long userId, String message) throws MessageDispatchException {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new MessageDispatchException("User wasn’t found"));
        String chatId = user.getTelegramChatId();
        if (chatId == null || chatId.isBlank()) {
            throw new IllegalArgumentException("User does not have a telegram chat ID");
        }

        String url = String.format("%s/bot%s/sendMessage", telegramApiBaseUrl, botToken);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("chat_id", chatId);
        params.add("text", message);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            restTemplate.postForEntity(url, request, String.class);
        } catch (RestClientException e) {
            throw new MessageDispatchException("Can't send a notification", e);
        }
    }

    @Override
    public void sentMessageSuccessesPayment(Payment payment) throws MessageDispatchException {
        String message = "The rent payment is successful!";
        sendMessage(payment.getRental().getUser().getId(), message);
    }

    @Override
    public void sentMessageCancelPayment(Payment payment) throws MessageDispatchException {
        String message = "The rent payment was cancelled!";
        sendMessage(payment.getRental().getUser().getId(), message);
    }

    @Override
    public void sentMessageCreateRental(Rental rental) throws MessageDispatchException {
        String message = String.format(
                "Congratulations! \n"
                        + "You have rented a car %s %s. \n"
                        + "Return date %s",
                rental.getCar().getBrand(),
                rental.getCar().getModel(),
                rental.getReturnDate()
        );
        sendMessage(rental.getUser().getId(), message);
    }

    @Override
    public void sentMessageClosedRental(Rental rental) throws MessageDispatchException {
        String message = String.format(
                "You have returned the car %s %s. \n"
                        + "We will be glad to see you again!!!",
                rental.getCar().getBrand(),
                rental.getCar().getModel()
        );
        sendMessage(rental.getUser().getId(), message);
    }

    @Override
    public void sentMessageOverdueRental(Rental rental) throws MessageDispatchException {
        String message = String.format(
                "You failed to return the car %s %s, %s! \n"
                        + "You have to pay a fine!!!!",
                rental.getCar().getBrand(),
                rental.getCar().getModel(),
                rental.getReturnDate()
        );
        sendMessage(rental.getUser().getId(), message);
    }

    @Override
    public void sentMessageNotOverdueRental(Rental rental) throws MessageDispatchException {
        String message = String.format(
                "Congratulations! \n"
                        + "We appreciate you using our cars! \n"
                        + "A reminder that your lease expires at %s",
                rental.getReturnDate()
        );
        sendMessage(rental.getUser().getId(), message);
    }

    @Override
    public void sentMessageToManagerOverdue(User user, Rental rental)
            throws MessageDispatchException {
        long days = ChronoUnit.DAYS.between(rental.getRentalDate(), LocalDate.now());
        String message = String.format(
                "Rental is overdue %s days\n"
                        + "Car - %s %s\n"
                        + "Customer - %s %s %s",
                days,
                rental.getCar().getBrand(),
                rental.getCar().getModel(),
                rental.getUser().getEmail(),
                rental.getUser().getFirstName(),
                rental.getUser().getLastName()
        );
        sendMessage(user.getId(), message);
    }

    @Override
    public void sentMessageToManagerNotOverdue(User user) throws MessageDispatchException {
        String message = "There are no rent arrears!";
        sendMessage(user.getId(), message);
    }
}
