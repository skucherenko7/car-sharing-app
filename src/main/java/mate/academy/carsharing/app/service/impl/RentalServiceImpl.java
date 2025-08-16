package mate.academy.carsharing.app.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mate.academy.carsharing.app.dto.rental.CreateRentalRequestDto;
import mate.academy.carsharing.app.dto.rental.RentalActualReturnDateResponseDto;
import mate.academy.carsharing.app.dto.rental.RentalResponseDto;
import mate.academy.carsharing.app.exception.EntityNotFoundException;
import mate.academy.carsharing.app.exception.ForbiddenOperationException;
import mate.academy.carsharing.app.exception.InsufficientQuantityException;
import mate.academy.carsharing.app.exception.MessageDispatchException;
import mate.academy.carsharing.app.mapper.RentalMapper;
import mate.academy.carsharing.app.model.Car;
import mate.academy.carsharing.app.model.Rental;
import mate.academy.carsharing.app.model.Role;
import mate.academy.carsharing.app.model.User;
import mate.academy.carsharing.app.repository.CarRepository;
import mate.academy.carsharing.app.repository.PaymentRepository;
import mate.academy.carsharing.app.repository.RentalRepository;
import mate.academy.carsharing.app.repository.UserRepository;
import mate.academy.carsharing.app.service.RentalService;
import mate.academy.carsharing.app.service.telegram.MessageDispatchService;
import mate.academy.carsharing.app.service.util.TimeProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {
    private static final boolean ACTIVE = true;
    private static final boolean INACTIVE = false;
    private static final int INVALID_LIMIT = 0;

    private final CarRepository carRepository;
    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;
    private final MessageDispatchService messageDispatchService;
    private final TimeProvider timeProvider;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public RentalResponseDto createRental(Authentication authentication,
                                          CreateRentalRequestDto requestDto) {

        User user = getUserFromAuthentication(authentication);
        checkUserHasNoActiveRental(user);

        Car car = getCarFromDB(requestDto.carId());
        checkCarInventory(car);

        Rental rental = buildRental(requestDto, user, car);

        sendCreateRentalMessage(rental, user);

        return rentalMapper.toResponseDto(rentalRepository.save(rental));
    }

    @Override
    public RentalResponseDto getRentalById(Long userId, Long rentalId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User"
                        + " not found with id: " + userId));

        boolean isManager = user.getRoles().stream()
                .anyMatch(role -> role.getName() == Role.RoleName.MANAGER);

        Rental rental;

        if (isManager) {
            rental = rentalRepository.findById(rentalId)
                    .orElseThrow(() -> new EntityNotFoundException("Rental"
                            + " not found with id: " + rentalId));
        } else {
            rental = rentalRepository.findByIdAndUserId(rentalId, userId)
                    .orElseThrow(() -> new ForbiddenOperationException("Access is denied"));
        }

        return rentalMapper.toResponseDto(rental);
    }

    @Override
    public Page<RentalResponseDto> findAllActiveRentals(Pageable pageable) {
        return rentalRepository.findByIsActiveTrue(pageable)
                .map(rentalMapper::toResponseDto);
    }

    @Override
    public RentalActualReturnDateResponseDto closeRental(Long userId, Long rentalId) {
        Rental rental = rentalRepository.findByIdAndUserId(rentalId, userId)
                .orElseThrow(()
                        -> new ForbiddenOperationException("Access is denied or rental not found"));

        if (!rental.getIsActive()) {
            throw new ForbiddenOperationException("The rental is closed");
        }

        rental.setIsActive(false);
        rental.setActualReturnDate(timeProvider.now());

        try {
            messageDispatchService.sentMessageClosedRental(rental);
        } catch (MessageDispatchException e) {
            log.info("Can't send the notification to user by id {}", userId);
        }

        Car car = rental.getCar();
        car.setInventory(car.getInventory() + 1);
        carRepository.save(car);

        rentalRepository.save(rental);

        return rentalMapper.toDtoWithActualReturnDate(rental);
    }

    private Car getCarFromDB(Long id) {
        return carRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can`t find the car by id " + id)
        );
    }

    private Rental getRentalFromDB(Long rentalId) {
        return rentalRepository.findById(rentalId)
                .orElseThrow(()
                        -> new EntityNotFoundException("Rental not found with id: " + rentalId));
    }

    private User getUserFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        String username;

        if (principal instanceof User u) {
            username = u.getEmail();
        } else if (principal instanceof UserDetails ud) {
            username = ud.getUsername();
        } else {
            username = principal.toString();
        }

        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private void checkUserHasNoActiveRental(User user) {
        if (rentalRepository.existsByUserIdAndIsActiveIsTrue(user.getId())) {
            throw new ForbiddenOperationException("You already have a rental car");
        }
    }

    private void checkCarInventory(Car car) {
        if (car.getInventory() == INVALID_LIMIT) {
            throw new InsufficientQuantityException("Insufficient quantity of cars");
        }
    }

    private Rental buildRental(CreateRentalRequestDto requestDto, User user, Car car) {
        Rental rental = rentalMapper.toModel(requestDto);
        rental.setRentalDate(timeProvider.now());
        rental.setReturnDate(requestDto.returnDate());
        rental.setIsActive(ACTIVE);
        rental.setUser(user);
        car.setInventory(car.getInventory() - 1);
        rental.setCar(car);
        return rental;
    }

    private void sendCreateRentalMessage(Rental rental, User user) {
        try {
            messageDispatchService.sentMessageCreateRental(rental);
        } catch (MessageDispatchException e) {
            log.info("Can't send the message to user by id {}", user.getId());
        }
    }
}
