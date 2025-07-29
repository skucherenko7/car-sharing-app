package mate.academy.carsharing.app.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Set;
import mate.academy.carsharing.app.dto.car.CreateCarDto;
import mate.academy.carsharing.app.model.Car;
import mate.academy.carsharing.app.model.Role;
import mate.academy.carsharing.app.model.User;
import mate.academy.carsharing.app.repository.CarRepository;
import mate.academy.carsharing.app.repository.RoleRepository;
import mate.academy.carsharing.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CarControllerTest {

    private static final String TEST_USER_EMAIL = "manager@gmail.com";

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CarRepository carRepository;

    private User testUser;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        carRepository.deleteAll();

        final Role role = roleRepository.save(new Role(Role.RoleName.MANAGER));

        testUser = new User();
        testUser.setEmail(TEST_USER_EMAIL);
        testUser.setPassword("Password111");
        testUser.setFirstName("manager");
        testUser.setLastName("manager");

        testUser.setRoles(Set.of(role));

        testUser.setTelegramChatId("1234567890");
        userRepository.save(testUser);
    }

    @Test
    @WithMockUser(username = TEST_USER_EMAIL, roles = "MANAGER")
    @DisplayName("CreateCar: should return created (201) and correct car data.")
    void createCar_shouldReturnCreated() throws Exception {
        CreateCarDto createDto = new CreateCarDto(
                "Q8",
                "Audi",
                Car.Type.SUV,
                5,
                BigDecimal.valueOf(900.00)
        );

        mockMvc.perform(post("/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.brand").value("Audi"))
                .andExpect(jsonPath("$.model").value("Q8"))
                .andExpect(jsonPath("$.type").value("SUV"))
                .andExpect(jsonPath("$.inventory").value(5))
                .andExpect(jsonPath("$.dailyFee").value(900.00));
    }

    @Test
    @WithMockUser(username = TEST_USER_EMAIL, roles = "MANAGER")
    @DisplayName("GetAllCars: should return OK (200) and list of cars.")
    void getAllCars_shouldReturnOk() throws Exception {
        Car car = new Car();
        car.setModel("X3");
        car.setBrand("BMW");
        car.setType(Car.Type.SUV);
        car.setInventory(2);
        car.setDailyFee(BigDecimal.valueOf(680.00));
        carRepository.save(car);

        mockMvc.perform(get("/cars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].brand").value("BMW"))
                .andExpect(jsonPath("$.content[0].model").value("X3"))
                .andExpect(jsonPath("$.content[0].type").value("SUV"))
                .andExpect(jsonPath("$.content[0].inventory").value(2))
                .andExpect(jsonPath("$.content[0].dailyFee").value(680.00));
    }

    @Test
    @WithMockUser(username = TEST_USER_EMAIL, roles = "MANAGER")
    @DisplayName("GetCarById: should return OK (200) and car details")
    void getCarById_shouldReturnCar() throws Exception {
        Car car = new Car();
        car.setModel("X3");
        car.setBrand("BMW");
        car.setType(Car.Type.SUV);
        car.setInventory(2);
        car.setDailyFee(BigDecimal.valueOf(680.00));
        car = carRepository.save(car);

        mockMvc.perform(get("/cars/" + car.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brand").value("BMW"))
                .andExpect(jsonPath("$.model").value("X3"))
                .andExpect(jsonPath("$.type").value("SUV"))
                .andExpect(jsonPath("$.inventory").value(2))
                .andExpect(jsonPath("$.dailyFee").value(680.00));
    }

    @Test
    @WithMockUser(username = TEST_USER_EMAIL, roles = "MANAGER")
    @DisplayName("UpdateCar: should return OK (200) with updated car.")
    void updateCar_shouldReturnUpdatedCar() throws Exception {
        Car car = new Car();
        car.setModel("Focus");
        car.setBrand("Ford");
        car.setType(Car.Type.SEDAN);
        car.setInventory(1);
        car.setDailyFee(BigDecimal.valueOf(480.00));
        car = carRepository.save(car);

        CreateCarDto updateDto = new CreateCarDto(
                "Fiesta",
                "Ford",
                Car.Type.HATCHBACK,
                4,
                BigDecimal.valueOf(500.00)
        );

        mockMvc.perform(put("/cars/" + car.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brand").value("Ford"))
                .andExpect(jsonPath("$.model").value("Fiesta"))
                .andExpect(jsonPath("$.type").value("HATCHBACK"))
                .andExpect(jsonPath("$.inventory").value(4))
                .andExpect(jsonPath("$.dailyFee").value(500.00));
    }

    @Test
    @WithMockUser(username = TEST_USER_EMAIL, roles = "MANAGER")
    @DisplayName("UpdateCarInventory: should update inventory with PATCH and return updated car.")
    void updateCarInventory_shouldUpdateInventory() throws Exception {
        Car car = new Car();
        car.setModel("Corolla");
        car.setBrand("Toyota");
        car.setType(Car.Type.SEDAN);
        car.setInventory(2);
        car.setDailyFee(BigDecimal.valueOf(350.00));
        car = carRepository.save(car);

        String patchBody = "{\"inventory\": 10}";

        mockMvc.perform(
                        patch("/cars/" + car.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(patchBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inventory").value(10));
    }

    @Test
    @WithMockUser(username = TEST_USER_EMAIL, roles = "MANAGER")
    @DisplayName("DeleteCar: should return No Content (204) after deletion.")
    void deleteCar_shouldReturnNoContent() throws Exception {
        Car car = new Car();
        car.setModel("Leaf");
        car.setBrand("Nissan");
        car.setType(Car.Type.HATCHBACK);
        car.setInventory(2);
        car.setDailyFee(BigDecimal.valueOf(670.00));
        car = carRepository.save(car);

        mockMvc.perform(delete("/cars/" + car.getId()))
                .andExpect(status().isNoContent());
    }
}
