package mate.academy.carsharing.app.service.telegram;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.app.exception.EntityNotFoundException;
import mate.academy.carsharing.app.exception.MessageDispatchException;
import mate.academy.carsharing.app.model.Payment;
import mate.academy.carsharing.app.model.Rental;
import mate.academy.carsharing.app.model.User;
import mate.academy.carsharing.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class MessageDispatchServiceImpl implements MessageDispatchService {
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Override
    public void sentMessage(Long userId, String message) throws MessageDispatchException {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("This user wasn’t found by id " + userId));
        String chatId = user.getTelegramChatId();
        if (chatId == null) {
            throw new MessageDispatchException("Chat id can`t be null");
        }

        String url = String.format("%s%s/sendMessage?chat_id=%s&text=%s",
                TELEGRAM_API_URL, botToken, chatId, message);
        try {
            restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            throw new MessageDispatchException("Can`t send a notification", e);
        }
    }

    @Override
    public void sentMessageSuccessesPayment(Payment payment) throws MessageDispatchException {
        String message = "The rent payment is successful!";
        sentMessage(payment.getRental().getUser().getId(), message);
    }

    @Override
    public void sentMessageCancelPayment(Payment payment) throws MessageDispatchException {
        String message = "The rent payment was cancelled!";
        sentMessage(payment.getRental().getUser().getId(), message);
    }

    @Override
    public void sentMessageCreateRental(Rental rental) throws MessageDispatchException {
        String message = String.format("""
                        Congratulations! \n
                        You have rented a car %s %s. \n
                        Return date %s
                        """,
                rental.getCar().getBrand(),
                rental.getCar().getModel(),
                rental.getReturnDate()
        );
        sentMessage(rental.getUser().getId(), message);
    }

    @Override
    public void sentMessageClosedRental(Rental rental) throws MessageDispatchException {
        String message = String.format("""
                        You have returned the car %s %s. \n
                        We will be glad to see you again!!!
                        """,
                rental.getCar().getBrand(),
                rental.getCar().getModel()
        );
        sentMessage(rental.getUser().getId(), message);
    }

    @Override
    public void sentMessageOverdueRental(Rental rental) throws MessageDispatchException {
        String message = String.format("""
                        You failed to return the car by %s %s, %s! \n
                        You have to pay a fine!!!!
                        """,
                rental.getCar().getBrand(),
                rental.getCar().getModel(),
                rental.getReturnDate()
        );
        sentMessage(rental.getUser().getId(), message);
    }

    @Override
    public void sentMessageNotOverdueRental(Rental rental) throws MessageDispatchException {
        String message = String.format("""
                        Congratulations! \n
                        We appreciate you using our cars! \n
                        A reminder that your lease expires at %s
                        """,
                rental.getReturnDate()
        );
        sentMessage(rental.getUser().getId(), message);
    }

    @Override
    public void sentMessageToManagerOverdue(User user, Rental rental)
            throws MessageDispatchException {
        long days = ChronoUnit.DAYS.between(rental.getRentalDate(), LocalDate.now());
        String message = String.format("""
                Rental is overdue %s days
                Car - %s %s
                Costumer - %s %s %s
                """,
                days,
                rental.getCar().getBrand(),
                rental.getCar().getModel(),
                rental.getUser().getEmail(),
                rental.getUser().getFirstName(),
                rental.getUser().getLastName()
        );
        sentMessage(user.getId(), message);
    }

    @Override
    public void sentMessageToManagerNotOverdue(User user) throws MessageDispatchException {
        String message = "There are no rent arrears!";
        sentMessage(user.getId(), message);

    }
}
