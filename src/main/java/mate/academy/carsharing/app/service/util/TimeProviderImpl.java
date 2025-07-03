package mate.academy.carsharing.app.service.util;

import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class TimeProviderImpl implements TimeProvider {
    @Override
    public LocalDate now() {
        return LocalDate.now();
    }
}
