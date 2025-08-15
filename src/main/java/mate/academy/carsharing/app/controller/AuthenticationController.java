package mate.academy.carsharing.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.app.dto.user.UserLoginRequestDto;
import mate.academy.carsharing.app.dto.user.UserLoginResponseDto;
import mate.academy.carsharing.app.dto.user.UserRegisterRequestDto;
import mate.academy.carsharing.app.dto.user.UserResponseDto;
import mate.academy.carsharing.app.exception.RegistrationException;
import mate.academy.carsharing.app.security.AuthenticationService;
import mate.academy.carsharing.app.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Endpoints for authentication users")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final UserService userService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/registration")
    @Operation(summary = "register user", description = "Registration a new user")
    public UserResponseDto register(@RequestBody @Valid UserRegisterRequestDto requestDto)
            throws RegistrationException {
        return userService.register(requestDto);
    }

    @PostMapping("/login")
    @Operation(summary = "login user", description = "Check if user is authenticated")
    public UserLoginResponseDto login(@RequestBody @Valid UserLoginRequestDto request) {
        return authenticationService.authenticate(request);
    }
}
