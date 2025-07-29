package mate.academy.carsharing.app.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import mate.academy.carsharing.app.dto.rental.CreateRentalRequestDto;
import mate.academy.carsharing.app.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(
        scripts = {
                "classpath:db/delete-all-data-db.sql",
                "classpath:db/roles/insert-roles.sql",
                "classpath:db/users/add-users-to-users-table.sql",
                "classpath:db/rentals/insert-cars.sql",
                "classpath:db/rentals/insert-cars-rentals-payments.sql"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
        scripts = "classpath:db/delete-all-data-db.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)

public class RentalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private String tokenCustomer;
    private String tokenManager;
    private String tokenNewCustomer;

    @BeforeEach
    void setUp() {
        UserDetails customer = new User(
                "veronika333@gmail.com",
                "",
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        tokenCustomer = jwtUtil.generateToken(customer);

        UserDetails manager = new User(
                "manager@gmail.com",
                "",
                List.of(new SimpleGrantedAuthority("ROLE_MANAGER"))
        );
        tokenManager = jwtUtil.generateToken(manager);

        UserDetails newCustomer = new User(
                "max222@gmail.com",
                "",
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        tokenNewCustomer = jwtUtil.generateToken(newCustomer);
    }

    @Test
    @DisplayName("CreateRental: should return Created (201) and rental data.")
    void createRental_shouldReturnCreatedRental() throws Exception {
        CreateRentalRequestDto requestDto = new CreateRentalRequestDto(
                LocalDate.now().plusDays(3),
                1L
        );

        mockMvc.perform(post("/rentals")
                        .header("Authorization", "Bearer " + tokenNewCustomer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.carId").value(1))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @DisplayName("GetRentalById: should return rental details for given ID.")
    void getRentalById_shouldReturnRental() throws Exception {
        mockMvc.perform(get("/rentals/1")
                        .header("Authorization", "Bearer " + tokenCustomer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.carId").value(1));
    }

    @Test
    @DisplayName("GetAllActiveRentals: should return paginated list of active rentals.")
    void getAllActiveRentals_shouldReturnPage() throws Exception {
        mockMvc.perform(get("/rentals/active")
                        .header("Authorization", "Bearer " + tokenManager)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].isActive").value(true));
    }

    @Test
    @DisplayName("CloseRental: should return rental with actualReturnDate set.")
    void closeRental_shouldReturnActualReturnDate() throws Exception {
        mockMvc.perform(post("/rentals/1/return")
                        .header("Authorization", "Bearer " + tokenCustomer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.actualReturnDate").exists());
    }
}
