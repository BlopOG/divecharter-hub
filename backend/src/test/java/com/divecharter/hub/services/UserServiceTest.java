package com.divecharter.hub.services;

import com.divecharter.hub.dto.CommonDtos.PageResponse;
import com.divecharter.hub.dto.UserDtos.UserResponse;
import com.divecharter.hub.exceptions.ResourceNotFoundException;
import com.divecharter.hub.models.User;
import com.divecharter.hub.models.enums.CertificationLevel;
import com.divecharter.hub.models.enums.Role;
import com.divecharter.hub.repositories.UserRepository;
import com.divecharter.hub.support.TestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private UserService userService;

    private final Pageable pageable = PageRequest.of(0, 20);

    @Test
    void listDivers_pendingOnly_returnsUnverifiedDivers() {
        User pending = TestData.diver(4L, CertificationLevel.OPEN_WATER, false);
        when(userRepository.findByRoleAndCertVerifiedFalse(Role.USER, pageable))
                .thenReturn(new PageImpl<>(List.of(pending), pageable, 1));

        PageResponse<UserResponse> result = userService.listDivers(true, pageable);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).certVerified()).isFalse();
    }

    @Test
    void listDivers_all_returnsEveryDiver() {
        when(userRepository.findByRole(Role.USER, pageable)).thenReturn(new PageImpl<>(List.of(
                TestData.diver(1L, CertificationLevel.OPEN_WATER, true),
                TestData.diver(2L, CertificationLevel.ADVANCED_OPEN_WATER, false)), pageable, 2));

        PageResponse<UserResponse> result = userService.listDivers(false, pageable);

        assertThat(result.totalElements()).isEqualTo(2);
    }

    @Test
    void setCertificationVerified_updatesDiver() {
        User diver = TestData.diver(4L, CertificationLevel.OPEN_WATER, false);
        when(userRepository.findById(4L)).thenReturn(Optional.of(diver));

        UserResponse response = userService.setCertificationVerified(4L, true);

        assertThat(response.certVerified()).isTrue();
        assertThat(diver.isCertVerified()).isTrue();
    }

    @Test
    void setCertificationVerified_throwsWhenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.setCertificationVerified(99L, true))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}