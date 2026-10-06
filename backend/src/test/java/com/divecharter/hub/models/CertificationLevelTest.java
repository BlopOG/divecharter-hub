package com.divecharter.hub.models;

import com.divecharter.hub.models.enums.CertificationLevel;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CertificationLevelTest {

    @ParameterizedTest
    @CsvSource({
            "OPEN_WATER, 18, true",
            "OPEN_WATER, 19, false",
            "ADVANCED_OPEN_WATER, 30, true",
            "ADVANCED_OPEN_WATER, 31, false",
            "DEEP_SPECIALTY, 40, true",
            "DIVEMASTER, 40, true"
    })
    void canDive_respectsDepthLimits(CertificationLevel level, int depth, boolean expected) {
        assertThat(level.canDive(depth)).isEqualTo(expected);
    }
}