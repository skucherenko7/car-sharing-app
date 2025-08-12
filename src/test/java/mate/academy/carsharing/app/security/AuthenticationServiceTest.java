package mate.academy.carsharing.app.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import mate.academy.carsharing.app.dto.UserLoginRequestDto;
import mate.academy.carsharing.app.dto.UserLoginResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class AuthenticationServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("authenticate(): should return JWT token when credentials are valid")
    void authenticate_shouldReturnJwtToken_whenCredentialsAreValid() {
        String email = "test@gmail.com";
        String password = "password";
        String expectedToken = "jwt-token";

        UserLoginRequestDto requestDto = new UserLoginRequestDto(email, password);

        UserDetails userDetails = User.withUsername(email)
                .password(password)
                .authorities("ROLE_CUSTOMER")
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken(userDetails)).thenReturn(expectedToken);

        UserLoginResponseDto response = authenticationService.authenticate(requestDto);

        assertEquals(expectedToken, response.token());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(userDetails);
    }
}
