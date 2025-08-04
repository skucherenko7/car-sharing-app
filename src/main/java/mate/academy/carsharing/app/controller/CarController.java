package mate.academy.carsharing.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.app.dto.car.CarDto;
import mate.academy.carsharing.app.dto.car.CreateCarDto;
import mate.academy.carsharing.app.dto.car.UpdateCarInventoryDto;
import mate.academy.carsharing.app.service.CarService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Car", description = "Endpoints for managing cars")
@RestController
@RequestMapping("/cars")
@SecurityRequirement(name = "BearerAuth")
@RequiredArgsConstructor
public class CarController {
    private final CarService carService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Save a car", description = "Saving a new car")
    public CarDto addCar(@RequestBody @Valid CreateCarDto createCarDto) {
        return carService.addCar(createCarDto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a car by id", description = "Viewing all information by car id")
    public CarDto findCarById(@PathVariable Long id) {
        return carService.findCarById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @Operation(summary = "Update a car by id",
            description = "It’s a possibility to change any parameter in the car")
    public CarDto updateCarById(@RequestBody @Valid CreateCarDto carDto, @PathVariable Long id) {
        return carService.updateCarById(carDto, id);
    }

    @GetMapping
    @Operation(summary = "View cars", description = "Viewing list of all cars information")
    public Page<CarDto> getAllCars(@ParameterObject Pageable pageable) {
        return carService.getAllCars(pageable);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @Operation(summary = "Update a car invention", description = "Updating a car invention by id")
    public CarDto updateCarsInventory(
            @RequestBody UpdateCarInventoryDto carDto, @PathVariable Long id) {
        return carService.updateCarInventory(carDto, id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @Operation(summary = "Delete a car by id", description = "Deleting the car by id")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCarById(@PathVariable Long id) {
        carService.deleteCarById(id);
    }
}
