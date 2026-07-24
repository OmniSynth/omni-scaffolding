package com.omni.scaffolding.common.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StringUtilsTest {

    @Test
    void inheritsApacheCommonsApis() {
        assertThat(StringUtils.isBlank("  ")).isTrue();
        assertThat(StringUtils.isNotBlank("omni")).isTrue();
        assertThat(StringUtils.trimToEmpty(null)).isEmpty();
    }

    @Test
    void formatAndCaseConvert() {
        assertThat(StringUtils.format("hello {}, age {}", "omni", 1)).isEqualTo("hello omni, age 1");
        assertThat(StringUtils.toUnderScoreCase("userName")).isEqualTo("user_name");
        assertThat(StringUtils.toCamelCase("user_name")).isEqualTo("userName");
    }

    @Test
    void helpers() {
        assertThat(StringUtils.nvl(null, "x")).isEqualTo("x");
        assertThat(StringUtils.blankToDefault("  ", "d")).isEqualTo("d");
        assertThat(StringUtils.equalsAnyIgnoreCase("Admin", "user", "ADMIN")).isTrue();
        assertThat(StringUtils.isHttpUrl("https://example.com")).isTrue();
        assertThat(StringUtils.splitTrim(" a, ,b ", ",")).containsExactly("a", "b");
        assertThat(StringUtils.isEmpty((List<?>) null)).isTrue();
        assertThat(StringUtils.ellipsis("abcdef", 5)).isEqualTo("ab...");
    }
}
