package mate.academy.carsharing.app.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import mate.academy.carsharing.app.dto.rental.CreateRentalRequestDto;
import mate.academy.carsharing.app.dto.rental.RentalActualReturnDateResponseDto;
import mate.academy.carsharing.app.dto.rental.RentalResponseDto;
import mate.academy.carsharing.app.exception.ForbiddenOperationException;
import mate.academy.carsharing.app.exception.InsufficientQuantityException;
import mate.academy.carsharing.app.model.Car;
import mate.academy.carsharing.app.model.Payment;
import mate.academy.carsharing.app.model.Role;
import mate.academy.carsharing.app.model.User;
import mate.academy.carsharing.app.repository.CarRepository;
import mate.academy.carsharing.app.repository.PaymentRepository;
import mate.academy.carsharing.app.repository.RentalRepository;
import mate.academy.carsharing.app.repository.RoleRepository;
import mate.academy.carsharing.app.repository.UserRepository;
import mate.academy.carsharing.app.service.impl.RentalServiceImpl;
import mate.academy.carsharing.app.service.util.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@SpringBootTest
public class RentalServiceTest {
    @Autowired
    private RentalServiceImpl rentalService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TimeProvider timeProvider;

    private User user;
    private Car car;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        rentalRepository.deleteAll();
        userRepository.deleteAll();
        carRepository.deleteAll();
        roleRepository.deleteAll();

        user = new User();
        user.setEmail("max222@gmail.com");
        user.setFirstName("Max");
        user.setLastName("Maxi");
        user.setPassword("Password222");
        user.setTelegramChatId("1234567892");

        final Role managerRole = roleRepository.save(new Role(Role.RoleName.MANAGER));
        final Role customerRole = roleRepository.save(new Role(Role.RoleName.CUSTOMER));
        user.setRoles(Set.of(customerRole));
        user = userRepository.save(user);

        car = new Car();
        car.setBrand("Toyota");
        car.setModel("Corolla");
        car.setType(Car.Type.SEDAN);
        car.setInventory(2);
        car.setDailyFee(new BigDecimal("250.00"));
        car = carRepository.save(car);

        authentication = new UsernamePasswordAuthenticationToken(user, null);
    }

    @Test
    @DisplayName("СreateRental: rental created successfully.")
    void createRental_success() {
        CreateRentalRequestDto requestDto = new CreateRentalRequestDto(
                LocalDate.now().plusDays(3),
                car.getId()
        );

        RentalResponseDto responseDto = rentalService.createRental(authentication, requestDto);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.carId()).isEqualTo(car.getId());

        Car carAfterRental = carRepository.findById(car.getId()).orElseThrow();
        assertThat(carAfterRental.getInventory()).isEqualTo(1);
    }

    @Test
    @DisplayName("CreateRental: throws ForbiddenOperationException if user"
            + " already has an active rental.")
    void createRental_shouldThrowIfUserHasActiveRental() {
        rentalService.createRental(authentication, new CreateRentalRequestDto(
                LocalDate.now().plusDays(2),
                car.getId()));

        CreateRentalRequestDto requestDto2 = new CreateRentalRequestDto(
                LocalDate.now().plusDays(4), car.getId());

        assertThrows(ForbiddenOperationException.class, () ->
                rentalService.createRental(authentication, requestDto2));
    }

    @Test
    @DisplayName("CreateRental: throws InsufficientQuantityException if car inventory is 0.")
    void createRental_shouldThrowIfCarInventoryIsZero() {
        car.setInventory(0);
        carRepository.save(car);

        CreateRentalRequestDto requestDto = new CreateRentalRequestDto(
                LocalDate.now().plusDays(3), car.getId());

        assertThrows(InsufficientQuantityException.class, () ->
                rentalService.createRental(authentication, requestDto));
    }

    @Test
    @DisplayName("GetRentalById: Returns the rental if the user has access.")
    void getRentalById_shouldReturnRentalResponse_whenUserHasAccess() {
        RentalResponseDto rentalResponse = rentalService.createRental(authentication,
                new CreateRentalRequestDto(LocalDate.now().plusDays(3), car.getId()));

        Long rentalId = rentalResponse.id();
        Long userId = user.getId();

        List<String> roles = List.of("ROLE_CUSTOMER");
        RentalResponseDto result = rentalService.getRentalById(userId, roles, rentalId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(rentalId);
    }

    @Test
    @DisplayName("GetRentalById: throws ForbiddenOperationException if user does not have access.")
    void getRentalById_shouldThrowForbidden_whenUserHasNoAccess() {
        Role customerRole = roleRepository.findByName(Role.RoleName.CUSTOMER)
                .orElseThrow(() -> new RuntimeException("Role CUSTOMER not found"));

        User user2 = new User();
        user2.setEmail("jim555@gmail.com");
        user2.setFirstName("Jim");
        user2.setLastName("Jil");
        user2.setPassword("Password555");
        user2.setTelegramChatId("1234567895");
        user2.setRoles(Set.of(customerRole));
        user2 = userRepository.save(user2);

        RentalResponseDto rentalResponse = rentalService.createRental(authentication,
                new CreateRentalRequestDto(LocalDate.now().plusDays(3), car.getId()));

        Long rentalId = rentalResponse.id();
        Long userId = user2.getId();

        List<String> roles = List.of("ROLE_CUSTOMER");

        assertThrows(ForbiddenOperationException.class, () ->
                rentalService.getRentalById(userId, roles, rentalId));
    }

    @Test
    @DisplayName("FindAllActiveRentals: returns the active rentals page.")
    void findAllActiveRentals_shouldReturnPageOfActiveRentals() {
        rentalService.createRental(authentication, new CreateRentalRequestDto(
                LocalDate.now().plusDays(3), car.getId()));

        Pageable pageable = PageRequest.of(0, 10);
        Page<RentalResponseDto> page = rentalService.findAllActiveRentals(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(page.getContent().stream().allMatch(RentalResponseDto::isActive)).isTrue();
    }

    @Test
    @DisplayName("CloseRental: closes rental and increases car inventory.")
    void closeRental_shouldCloseRentalAndIncreaseInventory() {
        RentalResponseDto rentalResponse = rentalService.createRental(authentication,
                new CreateRentalRequestDto(LocalDate.now().plusDays(3), car.getId()));

        Long rentalId = rentalResponse.id();
        final Long userId = user.getId();

        Payment payment = new Payment();
        payment.setRental(rentalRepository.findById(rentalId).orElseThrow());
        payment.setStatus(Payment.Status.PAID);
        payment.setAmount(car.getDailyFee());
        payment.setSessionId("test-session-id-123");
        payment.setSessionUrl("http://test-session-url");
        payment.setType(Payment.Type.PAYMENT);
        paymentRepository.save(payment);

        Car beforeClosingCar = carRepository.findById(car.getId()).orElseThrow();

        RentalActualReturnDateResponseDto result = rentalService.closeRental(userId, rentalId);

        assertThat(result).isNotNull();
        assertThat(result.actualReturnDate()).isEqualTo(LocalDate.now());

        Car carAfterClosing = carRepository.findById(car.getId()).orElseThrow();
        assertThat(carAfterClosing.getInventory()).isEqualTo(beforeClosingCar.getInventory() + 1);

        assertThrows(ForbiddenOperationException.class, () ->
                rentalService.closeRental(userId, rentalId));
    }

    @Test
    @DisplayName("CloseRental: throws ForbiddenOperationException if user does not have access.")
    void closeRental_shouldThrowForbidden_whenUserHasNoAccess() {
        final RentalResponseDto rentalResponse = rentalService.createRental(
                authentication,
                new CreateRentalRequestDto(LocalDate.now().plusDays(3), car.getId())
        );
        final Long rentalId = rentalResponse.id();

        final Role customerRole = roleRepository.findByName(Role.RoleName.CUSTOMER)
                .orElseThrow(() -> new RuntimeException("Role CUSTOMER not found"));

        final User unauthorizedUser = new User();
        unauthorizedUser.setEmail("den666@gmail.com");
        unauthorizedUser.setFirstName("Den");
        unauthorizedUser.setLastName("Dik");
        unauthorizedUser.setPassword("Password666");
        unauthorizedUser.setTelegramChatId("1234567896");
        unauthorizedUser.setRoles(Set.of(customerRole));

        final Long unauthorizedUserId = userRepository.save(unauthorizedUser).getId();

        assertThrows(ForbiddenOperationException.class, () ->
                rentalService.closeRental(unauthorizedUserId, rentalId));
    }

    @Test
    @DisplayName("CloseRental: throws ForbiddenOperationException if rental already closed")
    void closeRental_shouldThrow_whenRentalIsAlreadyClosed() {
        RentalResponseDto rental = rentalService.createRental(authentication,
                new CreateRentalRequestDto(LocalDate.now().plusDays(3), car.getId()));

        rentalService.closeRental(user.getId(), rental.id());

        assertThrows(ForbiddenOperationException.class, () ->
                rentalService.closeRental(user.getId(), rental.id()));
    }

    @Test
    @DisplayName("GetRentalById: throws exception if rental doesn't exist")
    void getRentalById_shouldThrow_whenRentalNotExist() {
        Long nonExistingRentalId = 999L;

        List<String> roles = List.of("ROLE_CUSTOMER");

        assertThrows(RuntimeException.class, () ->
                rentalService.getRentalById(user.getId(), roles, nonExistingRentalId));
    }

    @Test
    @DisplayName("CloseRental: throws exception if rental doesn't exist")
    void closeRental_shouldThrow_whenRentalNotExist() {
        Long nonExistingRentalId = 999L;

        assertThrows(RuntimeException.class, () ->
                rentalService.closeRental(user.getId(), nonExistingRentalId));
    }

    @Test
    @DisplayName("CreateRental: throws EntityNotFoundException when car not found")
    void createRental_shouldThrow_whenCarNotExist() {
        Long nonExistingCarId = 999L;
        CreateRentalRequestDto requestDto = new CreateRentalRequestDto(
                LocalDate.now().plusDays(3), nonExistingCarId
        );

        assertThrows(RuntimeException.class, () ->
                rentalService.createRental(authentication, requestDto));
    }
}
