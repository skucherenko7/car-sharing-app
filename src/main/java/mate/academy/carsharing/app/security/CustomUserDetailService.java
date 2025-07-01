package mate.academy.carsharing.app.security;

import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.app.exception.EntityNotFoundException;
import mate.academy.carsharing.app.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;

    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User with email "
                        + email + " not found"));
    }
}
