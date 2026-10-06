package com.divecharter.hub.models;

import com.divecharter.hub.models.enums.CertificationLevel;
import com.divecharter.hub.support.TestData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void diver_hasUserRoleAuthority() {
        User diver = TestData.diver(1L, CertificationLevel.OPEN_WATER, true);

        assertThat(diver.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
        assertThat(diver.getUsername()).isEqualTo("diver1@test.com");
        assertThat(diver.getPassword()).isEqualTo("hash");
    }

    @Test
    void admin_hasAdminRoleAuthority() {
        assertThat(TestData.admin(2L).getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
    }

    @Test
    void accountFlags_areAllEnabled() {
        User user = TestData.diver(1L, CertificationLevel.OPEN_WATER, true);

        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
        assertThat(user.isEnabled()).isTrue();
    }
}