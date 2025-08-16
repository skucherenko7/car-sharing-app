package mate.academy.carsharing.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import mate.academy.carsharing.app.dto.user.UserLoginRequestDto;
import mate.academy.carsharing.app.dto.user.UserLoginResponseDto;
import mate.academy.carsharing.app.dto.user.UserRegisterRequestDto;
import mate.academy.carsharing.app.dto.user.UserResponseDto;
import mate.academy.carsharing.app.example.UserUtilTest;
import mate.academy.carsharing.app.exception.CustomGlobalExceptionHandler;
import mate.academy.carsharing.app.exception.RegistrationException;
import mate.academy.carsharing.app.security.AuthenticationService;
import mate.academy.carsharing.app.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthenticationController authenticationController;

    @Mock
    private AuthenticationService authenticationService;

    private UserRegisterRequestDto userRegisterRequestDto;
    private UserResponseDto userResponseDto;
    private UserUtilTest userUtilTest;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController)
                .setControllerAdvice(new CustomGlobalExceptionHandler())
                .build();

        userRegisterRequestDto = UserUtilTest.getUserRequestDto();
        userResponseDto = UserUtilTest.getUserResponseDto();
    }

    @Test
    @DisplayName("RegisterUser: should return UserResponseDto when request is valid.")
    void registerUser_ShouldReturnUserResponseDto_WhenValidRequest() throws Exception {
        ArgumentCaptor<UserRegisterRequestDto> captor =
                ArgumentCaptor.forClass(UserRegisterRequestDto.class);
        UserResponseDto userResponseDto = UserUtilTest.getUserResponseDto();

        when(userService.register(captor.capture())).thenReturn(userResponseDto);

        mockMvc.perform(post("/auth/registration")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userRegisterRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userResponseDto.id()))
                .andExpect(jsonPath("$.email").value(userResponseDto.email()))
                .andExpect(jsonPath("$.firstName").value(userResponseDto.firstName()))
                .andExpect(jsonPath("$.lastName").value(userResponseDto.lastName()));

        assertEquals(userRegisterRequestDto.email(), captor.getValue().email());
        verify(userService).register(any(UserRegisterRequestDto.class));
    }

    @Test
    @DisplayName("RegisterUser: should throw RegistrationException when email already exists.")
    void registerUser_ShouldThrowRegistrationException_WhenEmailAlreadyExists() throws Exception {
        UserRegisterRequestDto userRegisterRequestDto = new UserRegisterRequestDto(
                "veronika333@gmail.com",
                "Password333",
                "Password333",
                "Veronika",
                "Verona",
                "1234567893"
        );

        when(userService.register(any(UserRegisterRequestDto.class)))
                .thenThrow(new RegistrationException("This email already exists"));

        mockMvc.perform(post("/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegisterRequestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.details[0]").value("This email already exists"));
    }

    @Test
    @DisplayName("LoginUser: should return token when credentials are valid.")
    void loginUser_ShouldReturnLoginResponse_WhenValidCredentials() throws Exception {
        UserLoginRequestDto loginRequestDto = new UserLoginRequestDto(
                "veronika333@gmail.com",
                "Password333"
        );

        UserLoginResponseDto loginResponseDto = new UserLoginResponseDto("fake-jwt-token");

        when(authenticationService.authenticate(any(UserLoginRequestDto.class)))
                .thenReturn(loginResponseDto);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"));
    }

    @Test
    @DisplayName("LoginUser: should return bad raquest (401) when credentials are invalid.")
    void loginUser_ShouldReturnBadRequest_WhenInvalidCredentials() throws Exception {
        UserLoginRequestDto loginRequestDto = new UserLoginRequestDto(
                "testUser@gmail.com",
                "wrongPassword"
        );

        when(authenticationService.authenticate(any(UserLoginRequestDto.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }
}
