package carsharing.app.service.telegram;

import carsharing.app.exception.MessageDispatchException;
import carsharing.app.model.Payment;
import carsharing.app.model.Rental;
import carsharing.app.model.User;

public interface MessageDispatchService {
    void sendMessage(Long userId, String message) throws MessageDispatchException;

    void sentMessageSuccessesPayment(Payment payment) throws MessageDispatchException;

    void sentMessageCancelPayment(Payment payment) throws MessageDispatchException;

    void sentMessageCreateRental(Rental rental) throws MessageDispatchException;

    void sentMessageClosedRental(Rental rental) throws MessageDispatchException;

    void sentMessageOverdueRental(Rental rental) throws MessageDispatchException;

    void sentMessageNotOverdueRental(Rental rental) throws MessageDispatchException;

    void sentMessageToManagerNotOverdue(User user) throws MessageDispatchException;

    void sentMessageToManagerOverdue(User user, Rental rental) throws MessageDispatchException;
}
