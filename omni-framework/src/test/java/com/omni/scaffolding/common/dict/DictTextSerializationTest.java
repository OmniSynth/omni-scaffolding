package com.omni.scaffolding.common.dict;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.omni.scaffolding.common.api.PageResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DictTextSerializationTest {

    @Test
    void writesSiblingTextForSingleValueAndReusesResponseLookup() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        ObjectMapper mapper = mapper((typeCode, value) -> {
            calls.incrementAndGet();
            return "1".equals(String.valueOf(value)) ? "启用" : null;
        });
        PageResult<StatusView> page = PageResult.of(1, 10, 2,
                List.of(new StatusView(1), new StatusView(1)));

        JsonNode json = mapper.readTree(mapper.writeValueAsString(page));

        assertEquals(1, json.at("/records/0/status").asInt());
        assertEquals("启用", json.at("/records/0/statusText").asText());
        assertEquals("启用", json.at("/records/1/statusText").asText());
        assertEquals(1, calls.get());
    }

    @Test
    void writesNullTextForNullOrUnknownValue() throws Exception {
        ObjectMapper mapper = mapper((typeCode, value) -> null);

        JsonNode unknown = mapper.readTree(mapper.writeValueAsString(new StatusView(9)));
        JsonNode empty = mapper.readTree(mapper.writeValueAsString(new StatusView(null)));

        assertTrue(unknown.get("statusText").isNull());
        assertTrue(empty.get("statusText").isNull());
    }

    @Test
    void rejectsGeneratedNameCollision() {
        ObjectMapper mapper = mapper((typeCode, value) -> "启用");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> mapper.writeValueAsString(new ConflictingView()));

        assertTrue(ex.getMessage().contains("statusText"));
    }

    private static ObjectMapper mapper(DictLabelResolver resolver) {
        SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new DictTextBeanSerializerModifier(resolver));
        return new ObjectMapper().registerModule(module);
    }

    private static final class StatusView {

        @DictText("sys_normal_disable")
        private final Integer status;

        private StatusView(Integer status) {
            this.status = status;
        }

        public Integer getStatus() {
            return status;
        }
    }

    private static final class ConflictingView {

        @DictText("sys_normal_disable")
        private final Integer status = 1;

        private final String statusText = "已有字段";

        public Integer getStatus() {
            return status;
        }

        public String getStatusText() {
            return statusText;
        }
    }
}
