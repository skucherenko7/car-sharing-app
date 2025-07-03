package mate.academy.carsharing.app.service.impl;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mate.academy.carsharing.app.dto.rental.CreateRentalRequestDto;
import mate.academy.carsharing.app.dto.rental.RentalActualReturnDateResponseDto;
import mate.academy.carsharing.app.dto.rental.RentalResponseDto;
import mate.academy.carsharing.app.dto.rental.UserRentalIsActiveRequestDto;
import mate.academy.carsharing.app.exception.EntityNotFoundException;
import mate.academy.carsharing.app.exception.ForbiddenOperationException;
import mate.academy.carsharing.app.exception.InsufficientQuantityException;
import mate.academy.carsharing.app.exception.MessageDispatchException;
import mate.academy.carsharing.app.mapper.RentalMapper;
import mate.academy.carsharing.app.model.Car;
import mate.academy.carsharing.app.model.Rental;
import mate.academy.carsharing.app.model.User;
import mate.academy.carsharing.app.repository.CarRepository;
import mate.academy.carsharing.app.repository.RentalRepository;
import mate.academy.carsharing.app.service.RentalService;
import mate.academy.carsharing.app.service.telegram.MessageDispatchService;
import mate.academy.carsharing.app.service.util.TimeProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
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

    @Override
    @Transactional
    public RentalResponseDto createRental(
            Authentication authentication, CreateRentalRequestDto requestDto) {
        User user = (User) authentication.getPrincipal();
        if (rentalRepository.existsByUserIdAndIsActiveIsTrue(user.getId())) {
            throw new ForbiddenOperationException("You already have a rental car");
        }
        Car car = getCarFromDB(requestDto.carId());
        if (car.getInventory() == INVALID_LIMIT) {
            throw new InsufficientQuantityException(
                    "Insufficient quantity of cars");
        }
        Rental rental = rentalMapper.toModel(requestDto);
        rental.setRentalDate(timeProvider.now());
        rental.setReturnDate(requestDto.returnDate());
        rental.setIsActive(ACTIVE);
        rental.setUser(user);
        car.setInventory(car.getInventory() - 1);
        rental.setCar(car);
        try {
            messageDispatchService.sentMessageCreateRental(rental);
        } catch (MessageDispatchException e) {
            log.info("Can`t send the notification to user by id {}", user.getId());
        }
        return rentalMapper.toResponseDto(rentalRepository.save(rental));
    }

    @Override
    public RentalResponseDto getRentalById(Long userId, Long rentalId) {
        List<Rental> rentals = rentalRepository.findAllByUserId(userId);
        Rental rental = getRentalFromDB(rentalId);
        if (!rentals.contains(rental)) {
            throw new ForbiddenOperationException("Access is denied");
        }
        return rentalMapper.toResponseDto(rental);
    }

    @Override
    public Page<RentalResponseDto> findActiveRentalsForUserRequest(
            UserRentalIsActiveRequestDto requestDto, Pageable pageable) {
        return rentalRepository
                .findAllByUserIdAndIsActive(requestDto.userId(), requestDto.isActive(), pageable)
                .map(rentalMapper::toResponseDto);

    }

    @Override
    @Transactional
    public RentalActualReturnDateResponseDto closeRental(Long userId, Long rentalId) {
        List<Rental> rentals = rentalRepository.findAllByUserId(userId);
        Rental rental = getRentalFromDB(rentalId);
        if (!rental.getIsActive()) {
            throw new ForbiddenOperationException("The rental is closed");
        }
        if (!rentals.contains(rental)) {
            throw new ForbiddenOperationException("Access is denied");
        }
        rental.setIsActive(INACTIVE);
        rental.setActualReturnDate(LocalDate.now());
        try {
            messageDispatchService.sentMessageClosedRental(rental);
        } catch (MessageDispatchException e) {
            log.info("Can`t send the notification to user by id {}", userId);
        }
        Car car = rental.getCar();
        car.setInventory(car.getInventory() + 1);
        carRepository.save(car);
        return rentalMapper.toDtoWithActualReturnDate(rental);
    }

    private Car getCarFromDB(Long id) {
        return carRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can`t find the car by id " + id)
        );
    }

    private Rental getRentalFromDB(Long id) {
        return rentalRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can`t find the rental by id " + id)
        );
    }
}
