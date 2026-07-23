package com.omni.scaffolding.security.xss;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class XssHttpServletRequestWrapperTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void cleansQueryParameter() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("q", "<script>alert(1)</script>ok");

        XssHttpServletRequestWrapper wrapped =
                new XssHttpServletRequestWrapper(request, XssMode.STRIP, objectMapper);

        assertThat(wrapped.getParameter("q")).isEqualTo("ok");
    }

    @Test
    void cleansJsonBodyRecursively() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("application/json");
        request.setContent("{\"name\":\"<script>x</script>Ada\",\"tags\":[\"a\",\"<img onerror=1>\"]}"
                .getBytes(StandardCharsets.UTF_8));

        XssHttpServletRequestWrapper wrapped =
                new XssHttpServletRequestWrapper(request, XssMode.STRIP, objectMapper);

        JsonNode node = objectMapper.readTree(wrapped.getInputStream());
        assertThat(node.get("name").asText()).isEqualTo("Ada");
        assertThat(node.get("tags").get(1).asText()).doesNotContain("onerror");
    }

    @Test
    void skipsAuthorizationHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer <token>");
        request.addHeader("X-Custom", "<script>1</script>v");

        XssHttpServletRequestWrapper wrapped =
                new XssHttpServletRequestWrapper(request, XssMode.STRIP, objectMapper);

        assertThat(wrapped.getHeader("Authorization")).isEqualTo("Bearer <token>");
        assertThat(wrapped.getHeader("X-Custom")).isEqualTo("v");
    }
}
