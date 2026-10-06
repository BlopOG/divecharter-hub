package com.divecharter.hub.security;

import com.divecharter.hub.models.User;
import com.divecharter.hub.models.enums.CertificationLevel;
import com.divecharter.hub.repositories.UserRepository;
import com.divecharter.hub.support.TestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private CustomUserDetailsService service;

    @Test
    void loadUserByUsername_normalizesEmailAndReturnsUser() {
        User user = TestData.diver(1L, CertificationLevel.OPEN_WATER, true);
        when(userRepository.findByEmail("diver1@test.com")).thenReturn(Optional.of(user));

        assertThat(service.loadUserByUsername("  Diver1@Test.com ")).isSameAs(user);
    }

    @Test
    void loadUserByUsername_throwsWhenMissing() {
        when(userRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("nobody@test.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}