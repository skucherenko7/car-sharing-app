package mate.academy.carsharing.app.mapper;

import java.util.Set;
import javax.annotation.processing.Generated;
import mate.academy.carsharing.app.dto.UpdateUserRequestDto;
import mate.academy.carsharing.app.dto.UserRegisterRequestDto;
import mate.academy.carsharing.app.dto.user.UserDto;
import mate.academy.carsharing.app.dto.user.UserResponseDto;
import mate.academy.carsharing.app.model.User;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-12T19:52:46+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toModel(UserRegisterRequestDto requestDto) {
        if ( requestDto == null ) {
            return null;
        }

        User user = new User();

        user.setEmail( requestDto.email() );
        user.setFirstName( requestDto.firstName() );
        user.setLastName( requestDto.lastName() );
        user.setPassword( requestDto.password() );
        user.setTelegramChatId( requestDto.telegramChatId() );

        return user;
    }

    @Override
    public UserDto toDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserDto userDto = new UserDto();

        userDto.setTelegramChatId( user.getTelegramChatId() );
        userDto.setId( user.getId() );
        userDto.setEmail( user.getEmail() );
        userDto.setFirstName( user.getFirstName() );
        userDto.setLastName( user.getLastName() );

        setRolesId( userDto, user );

        return userDto;
    }

    @Override
    public UserResponseDto toResponseDto(User user) {
        if ( user == null ) {
            return null;
        }

        Long id = null;
        String email = null;
        String firstName = null;
        String lastName = null;
        String telegramChatId = null;

        id = user.getId();
        email = user.getEmail();
        firstName = user.getFirstName();
        lastName = user.getLastName();
        telegramChatId = user.getTelegramChatId();

        Set<String> roles = getRoleNames(user.getRoles());

        UserResponseDto userResponseDto = new UserResponseDto( id, email, firstName, lastName, telegramChatId, roles );

        return userResponseDto;
    }

    @Override
    public void updateUser(User user, UpdateUserRequestDto requestDto) {
        if ( requestDto == null ) {
            return;
        }

        user.setEmail( requestDto.email() );
        user.setFirstName( requestDto.firstName() );
        user.setLastName( requestDto.lastName() );
        user.setTelegramChatId( requestDto.telegramChatId() );
    }
}
