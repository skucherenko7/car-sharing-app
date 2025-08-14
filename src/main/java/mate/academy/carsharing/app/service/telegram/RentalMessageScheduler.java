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
import org.springframework.stereotype.Component;

@Component
public class RentalMessageScheduler {

    private final RentalRepository rentalRepository;
    private final MessageDispatchService messageDispatchService;
    private final UserRepository userRepository;

    public RentalMessageScheduler(RentalRepository rentalRepository,
                                  MessageDispatchService messageDispatchService,
                                  UserRepository userRepository) {
        this.rentalRepository = rentalRepository;
        this.messageDispatchService = messageDispatchService;
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void messageOverdueRents() throws MessageDispatchException {
        List<Rental> overdueRentals = rentalRepository
                .findAllByReturnDateLessThan(LocalDate.now());
        List<User> managers = userRepository.findAllByRoles_Name(Role.RoleName.MANAGER);

        if (overdueRentals.isEmpty()) {
            for (User manager : managers) {
                messageDispatchService.sentMessageToManagerNotOverdue(manager);
            }
            return;
        }

        for (Rental rental : overdueRentals) {
            for (User manager : managers) {
                messageDispatchService.sentMessageToManagerOverdue(manager, rental);
            }
            messageDispatchService.sentMessageOverdueRental(rental);
        }
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void messageNotOverdueRents() throws MessageDispatchException {
        List<Rental> activeRentals = rentalRepository
                .findAllByReturnDateGreaterThanEqual(LocalDate.now());
        for (Rental rental : activeRentals) {
            messageDispatchService.sentMessageNotOverdueRental(rental);
        }
    }
}
