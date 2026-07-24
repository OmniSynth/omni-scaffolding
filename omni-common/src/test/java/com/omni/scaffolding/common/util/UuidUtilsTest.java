package com.omni.scaffolding.common.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UuidUtilsTest {

    @Test
    void randomUuidHasHyphenForm() {
        String id = UuidUtils.randomUuid();
        assertThat(id).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        assertThat(UuidUtils.randomUuid()).isNotEqualTo(id);
    }

    @Test
    void simpleUuidIs32Hex() {
        String id = UuidUtils.simpleUuid();
        assertThat(id).matches("[0-9a-f]{32}").doesNotContain("-");
    }

    @Test
    void simpleUuidWithPrefix() {
        assertThat(UuidUtils.simpleUuid("file_")).startsWith("file_").hasSize(5 + 32);
        assertThat(UuidUtils.simpleUuid("  ")).matches("[0-9a-f]{32}");
    }
}
