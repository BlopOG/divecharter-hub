package com.divecharter.hub.services;

import com.divecharter.hub.dto.UserDtos.AuthResponse;
import com.divecharter.hub.dto.UserDtos.LoginRequest;
import com.divecharter.hub.dto.UserDtos.RegisterRequest;
import com.divecharter.hub.dto.UserDtos.UserResponse;
import com.divecharter.hub.exceptions.DuplicateResourceException;
import com.divecharter.hub.exceptions.ResourceNotFoundException;
import com.divecharter.hub.models.User;
import com.divecharter.hub.models.enums.CertificationLevel;
import com.divecharter.hub.models.enums.Role;
import com.divecharter.hub.repositories.UserRepository;
import com.divecharter.hub.security.JwtService;
import com.divecharter.hub.support.TestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;

    @InjectMocks private AuthService authService;

    @Test
    void register_createsUnverifiedUserWithHashedPassword() {
        when(userRepository.existsByEmail("new@diver.com")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = authService.register(new RegisterRequest(
                "  New@Diver.com ", "Password1", "<b>New</b> Diver",
                CertificationLevel.OPEN_WATER, "PADI", "123"));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("new@diver.com");
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("bcrypt-hash");
        assertThat(saved.getValue().getFullName()).isEqualTo("New Diver");
        assertThat(saved.getValue().getRole()).isEqualTo(Role.USER);
        assertThat(saved.getValue().isCertVerified()).isFalse();
        assertThat(response.maxDepthMeters()).isEqualTo(18);
    }

    @Test
    void register_rejectsDuplicateEmail() {
        when(userRepository.existsByEmail("taken@diver.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest(
                "taken@diver.com", "Password1", "Taken", CertificationLevel.OPEN_WATER, null, null)))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_returnsTokenForValidCredentials() {
        User user = TestData.diver(1L, CertificationLevel.OPEN_WATER, true);
        when(userRepository.findByEmail("diver1@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(new LoginRequest("Diver1@Test.com", "Password1"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.user().email()).isEqualTo("diver1@test.com");
    }

    @Test
    void login_propagatesBadCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("x@y.com", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void getCurrentUser_throwsWhenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getCurrentUser(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}