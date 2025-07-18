package mate.academy.carsharing.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.app.dto.UpdateUserPasswordRequestDto;
import mate.academy.carsharing.app.dto.UpdateUserRequestDto;
import mate.academy.carsharing.app.dto.user.UpdateUserRoleRequestDto;
import mate.academy.carsharing.app.dto.user.UserDto;
import mate.academy.carsharing.app.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "Endpoints for managing users")
@RestController
@RequestMapping("/users")
@SecurityRequirement(name = "BearerAuth")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "View the user", description = "View the user information")
    public UserDto getUserById(Authentication authentication) {
        return userService.findUserById(getUserId(authentication));
    }

    @PutMapping("/me")
    @Operation(summary = "Update the user", description = "Updating user information")
    public UserDto updateUser(
            Authentication authentication, @RequestBody @Valid UpdateUserRequestDto requestDto) {
        return userService.updateUser(getUserId(authentication), requestDto);
    }

    @PatchMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Update the password of user",
            description = "Updating the password of user")
    public void updateUserPassword(
            Authentication authentication,
            @RequestBody @Valid UpdateUserPasswordRequestDto requestDto) {
        userService.updateUserPassword(getUserId(authentication), requestDto);
    }

    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @PutMapping("/{id}/role")
    @Operation(summary = "Update role", description = "Updating the role of user")
    public UserDto updateUserRole(
            @PathVariable Long id, @RequestBody @Valid UpdateUserRoleRequestDto requestDto) {
        return userService.updateUserRole(id, requestDto);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public Page<UserDto> getAllUsers(
            @RequestParam(required = false) String sort,
            @PageableDefault(sort = "id") Pageable pageable) {
        if (sort != null && sort.contains("[")) {
            pageable = Pageable.unpaged();
        }
        return userService.getAllUsers(pageable);
    }

    @Operation(
            summary = "Get user ID from authentication",
            description = "Extracts the user ID from the authenticated principal object"
    )
    private Long getUserId(Authentication authentication) {
        String email = (String) authentication.getPrincipal();

        UserDto user = userService.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return user.getId();
    }
}
