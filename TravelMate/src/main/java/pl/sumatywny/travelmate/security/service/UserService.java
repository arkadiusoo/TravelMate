package pl.sumatywny.travelmate.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.sumatywny.travelmate.security.model.User;
import pl.sumatywny.travelmate.security.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
    /**
     * Finds user ID by email address
     * @param email The email to search for
     * @return UUID of the user or null if not found
     */
    public UUID findUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElse(null);
    }

    /**
     * Finds email by user ID
     * @param userId The user ID to search for
     * @return Email of the user or null if not found
     */
    public String findEmailByUserId(UUID userId) {
        return userRepository.findById(userId)
                .map(User::getEmail)
                .orElse(null);
    }

    /**
     * Validates that a user exists by email
     * @param email The email to check
     * @return true if user exists, false otherwise
     */
    public boolean isRegisteredUser(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Gets current user ID from authentication context
     * @param authentication Current authentication object
     * @return UUID of the current user
     * @throws IllegalStateException if user not authenticated or not found
     */
    public UUID getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new IllegalStateException("User not found with email: " + email));
    }
}