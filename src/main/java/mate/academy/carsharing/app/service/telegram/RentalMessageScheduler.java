package mate.academy.carsharing.app.service.telegram;

import java.time.LocalDate;
import java.util.List;
import mate.academy.carsharing.app.exception.MessageDispatchException;
import mate.academy.carsharing.app.model.Rental;
import mate.academy.carsharing.app.model.Role;
import mate.academy.carsharing.app.model.User;
import mate.academy.carsharing.app.repository.RentalRepository;
import mate.academy.carsharing.app.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;

public class RentalMessageScheduler {
    private RentalRepository rentalRepository;
    private MessageDispatchService messageDispatchService;
    private UserRepository userRepository;

    @Scheduled(cron = "0 0 9 * * *")
    public void messageNotOverdueRents() throws MessageDispatchException {
        List<Rental> rentals = rentalRepository.findAllByReturnDateLessThan(LocalDate.now());
        for (Rental rental : rentals) {
            messageDispatchService.sentMessageNotOverdueRental(rental);
        }
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void messageOverdueRents() throws MessageDispatchException {
        List<Rental> rentals =
                rentalRepository.findAllByReturnDateGreaterThanEqual(LocalDate.now());
        List<User> users = userRepository.findAllByRoles_Role(Role.RoleName.MANAGER);
        if (rentals.isEmpty()) {
            for (User user : users) {
                messageDispatchService.sentMessageToManagerNotOverdue(user);
            }
            return;
        }
        for (Rental rental : rentals) {
            for (User user : users) {
                messageDispatchService.sentMessageToManagerOverdue(user, rental);
            }
            messageDispatchService.sentMessageOverdueRental(rental);
        }
    }
}
