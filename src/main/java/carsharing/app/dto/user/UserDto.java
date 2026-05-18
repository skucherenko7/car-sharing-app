package carsharing.app.dto.user;

import java.util.List;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String telegramChatId;
    private List<Long> rolesId;
}
