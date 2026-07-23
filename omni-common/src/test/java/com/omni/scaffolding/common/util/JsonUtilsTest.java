package com.omni.scaffolding.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonUtilsTest {

    @Test
    void toJsonAndFromJson() {
        Sample sample = new Sample("omni", Instant.parse("2026-07-22T08:00:00Z"));
        String json = JsonUtils.toJson(sample);
        assertThat(json).contains("\"name\":\"omni\"").contains("2026-07-22T08:00:00Z");

        Sample parsed = JsonUtils.fromJson(json, Sample.class);
        assertThat(parsed.name()).isEqualTo("omni");
        assertThat(parsed.time()).isEqualTo(Instant.parse("2026-07-22T08:00:00Z"));
    }

    @Test
    void fromJsonTypeReference() {
        String json = "[{\"k\":1},{\"k\":2}]";
        List<Map<String, Integer>> list = JsonUtils.fromJson(json, new TypeReference<>() {
        });
        assertThat(list).hasSize(2);
        assertThat(list.get(0).get("k")).isEqualTo(1);
    }

    @Test
    void convertAndParseTree() {
        Map<String, Object> map = Map.of("name", "x");
        Sample sample = JsonUtils.convert(map, Sample.class);
        assertThat(sample.name()).isEqualTo("x");
        assertThat(JsonUtils.parseTree("{\"a\":1}").get("a").asInt()).isEqualTo(1);
    }

    @Test
    void blankAndInvalid() {
        assertThat(JsonUtils.toJson(null)).isNull();
        assertThat(JsonUtils.fromJson("  ", Sample.class)).isNull();
        assertThat(JsonUtils.looksLikeJson("{\"a\":1}")).isTrue();
        assertThat(JsonUtils.looksLikeJson("plain")).isFalse();
        assertThatThrownBy(() -> JsonUtils.fromJson("{bad", Sample.class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    record Sample(String name, Instant time) {
    }
}
