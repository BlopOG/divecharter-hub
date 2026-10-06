package com.divecharter.hub.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InputSanitizerTest {

    @Test
    void clean_removesHtmlTagsAndTrims() {
        assertThat(InputSanitizer.clean("  <script>alert(1)</script>Evil Reef  ")).isEqualTo("alert(1)Evil Reef");
    }

    @Test
    void clean_turnsBlankIntoNull() {
        assertThat(InputSanitizer.clean("   ")).isNull();
        assertThat(InputSanitizer.clean("<b></b>")).isNull();
        assertThat(InputSanitizer.clean(null)).isNull();
    }

    @Test
    void clean_leavesNormalTextAlone() {
        assertThat(InputSanitizer.clean("Molasses Reef")).isEqualTo("Molasses Reef");
    }
}
