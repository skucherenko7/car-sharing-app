package carsharing.app.service.util;

import java.time.LocalDate;

public interface TimeProvider {
    LocalDate now();
}
