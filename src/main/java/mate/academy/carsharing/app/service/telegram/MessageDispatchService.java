package mate.academy.carsharing.app.service.telegram;

import mate.academy.carsharing.app.exception.MessageDispatchException;
import mate.academy.carsharing.app.model.Payment;
import mate.academy.carsharing.app.model.Rental;
import mate.academy.carsharing.app.model.User;

public interface MessageDispatchService {
    void sentMessage(Long userId, String message) throws MessageDispatchException;

    void sentMessageSuccessesPayment(Payment payment) throws MessageDispatchException;

    void sentMessageCancelPayment(Payment payment) throws MessageDispatchException;

    void sentMessageCreateRental(Rental rental) throws MessageDispatchException;

    void sentMessageClosedRental(Rental rental) throws MessageDispatchException;

    void sentMessageOverdueRental(Rental rental) throws MessageDispatchException;

    void sentMessageNotOverdueRental(Rental rental) throws MessageDispatchException;

    void sentMessageToManagerNotOverdue(User user) throws MessageDispatchException;

    void sentMessageToManagerOverdue(User user, Rental rental) throws MessageDispatchException;
}
