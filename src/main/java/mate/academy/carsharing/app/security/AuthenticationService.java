package mate.academy.carsharing.app.security;

import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.app.dto.UserLoginRequestDto;
import mate.academy.carsharing.app.dto.UserLoginResponseDto;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public UserLoginResponseDto authenticate(UserLoginRequestDto request) {
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(),
                        request.password())
        );

        String token = jwtUtil.generatedToken(authentication.getName());
        return new UserLoginResponseDto(token);
    }
}
