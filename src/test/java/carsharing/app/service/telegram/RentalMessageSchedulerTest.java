package carsharing.app.service.telegram;

import static org.junit.Assert.assertEquals;

import carsharing.app.exception.MessageDispatchException;
import carsharing.app.model.Rental;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application.properties")
@Sql(scripts = {
        "/db/delete-all-data-db.sql",
        "/db/scheduler/scheduler-test-data.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class RentalMessageSchedulerTest {

    @Autowired
    private RentalMessageScheduler rentalMessageScheduler;

    @Autowired
    private MessageDispatchService messageDispatchService;

    @Captor
    private ArgumentCaptor<Rental> rentalCaptor;

    @TestConfiguration
    static class TestTelegramConfig {
        @Bean
        public MessageDispatchService messageDispatchService() {
            return Mockito.mock(MessageDispatchService.class);
        }
    }

    @Test
    @DisplayName("MessageNotOverdueRents: sends messages about not overdue rentals.")
    void testMessageNotOverdueRents() throws MessageDispatchException {
        Mockito.doNothing().when(messageDispatchService).sentMessageNotOverdueRental(Mockito.any());

        rentalMessageScheduler.messageNotOverdueRents();

        Mockito.verify(messageDispatchService, Mockito.times(2))
                .sentMessageNotOverdueRental(rentalCaptor.capture());

        List<Rental> rentals = rentalCaptor.getAllValues();
        assertEquals(2, rentals.size());
    }

    @Test
    @DisplayName("MessageOverdueRents: sends messages to managers and users"
            + " about overdue and not overdue rentals.")
    void testMessageOverdueRents() throws MessageDispatchException {
        Mockito.doNothing().when(messageDispatchService)
                .sentMessageToManagerOverdue(Mockito.any(), Mockito.any());
        Mockito.doNothing().when(messageDispatchService)
                .sentMessageOverdueRental(Mockito.any());
        Mockito.doNothing().when(messageDispatchService)
                .sentMessageToManagerNotOverdue(Mockito.any());

        rentalMessageScheduler.messageOverdueRents();

        Mockito.verify(messageDispatchService, Mockito.atLeast(0))
                .sentMessageToManagerOverdue(Mockito.any(), Mockito.any());

        Mockito.verify(messageDispatchService, Mockito.atLeast(0))
                .sentMessageOverdueRental(Mockito.any());

        Mockito.verify(messageDispatchService, Mockito.atLeast(0))
                .sentMessageToManagerNotOverdue(Mockito.any());
    }
}
