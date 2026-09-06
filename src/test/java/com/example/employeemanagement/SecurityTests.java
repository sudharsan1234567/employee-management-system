package com.example.employeemanagement;

import com.example.employeemanagement.config.CustomUserDetailsService;
import com.example.employeemanagement.config.SecurityConfig;
import com.example.employeemanagement.entity.Role;
import com.example.employeemanagement.entity.User;
import com.example.employeemanagement.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class SecurityTests {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void contextLoads() {
        // Verifies the full Spring context loads without errors
    }

    @Test
    void passwordEncoderBeanIsAvailable() {
        assertThat(passwordEncoder).isNotNull();
    }

    @Test
    void passwordEncoderUsesBCrypt() {
        String raw = "testPassword123";
        String encoded = passwordEncoder.encode(raw);
        // BCrypt hashes always start with $2a$, $2b$, or $2y$
        assertThat(encoded).startsWith("$2");
        assertThat(passwordEncoder.matches(raw, encoded)).isTrue();
    }

    @Test
    void passwordIsNeverStoredAsPlainText() {
        String raw = "testPassword123";
        String encoded = passwordEncoder.encode(raw);
        assertThat(encoded).isNotEqualTo(raw);
    }

    @Test
    void unknownUsernameThrowsUsernameNotFoundException() {
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("this_user_does_not_exist_xyz"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void adminUserIsCreatedOnStartup() {
        // The AdminUserInitializer should have inserted an admin on first run
        assertThat(userRepository.findByUsername("admin")).isPresent();
    }

    @Test
    void adminUserPasswordIsBCryptHashed() {
        User admin = userRepository.findByUsername("admin").orElseThrow();
        // Password must be BCrypt encoded, not plain text
        assertThat(admin.getPassword()).startsWith("$2");
    }

    @Test
    void adminUserHasCorrectRole() {
        User admin = userRepository.findByUsername("admin").orElseThrow();
        assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void userDetailsServiceCanLoadAdminUser() {
        // Admin is created by AdminUserInitializer at startup
        var userDetails = userDetailsService.loadUserByUsername("admin");
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("admin");
        assertThat(userDetails.getAuthorities())
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
