package carsharing.app.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponseDto(
        int code,
        String message,
        List<String> details,
        LocalDateTime timestamp
) {}
