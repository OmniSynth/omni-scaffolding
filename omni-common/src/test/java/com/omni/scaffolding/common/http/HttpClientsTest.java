package com.omni.scaffolding.common.http;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

class HttpClientsTest {

    private static HttpServer server;
    private static String baseUrl;

    @BeforeAll
    static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/echo", exchange -> {
            byte[] req = exchange.getRequestBody().readAllBytes();
            String method = exchange.getRequestMethod();
            String query = exchange.getRequestURI().getRawQuery();
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            String body = method + "|" + (query == null ? "" : query) + "|"
                    + (contentType == null ? "" : contentType) + "|"
                    + new String(req, StandardCharsets.UTF_8);
            byte[] resp = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("X-Echo", "ok");
            exchange.sendResponseHeaders(200, resp.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(resp);
            }
        });
        server.createContext("/json", exchange -> {
            byte[] resp = "{\"name\":\"omni\",\"count\":2}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, resp.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(resp);
            }
        });
        server.createContext("/fail", exchange -> {
            byte[] resp = "boom".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, resp.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(resp);
            }
        });
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void getWithQueryAndTimeout() {
        HttpResult result = HttpClients.get(baseUrl + "/echo")
                .query("q", "hello world")
                .connectTimeout(Duration.ofSeconds(2))
                .requestTimeout(Duration.ofSeconds(5))
                .executeOk();

        assertThat(result.header("X-Echo")).contains("ok");
        assertThat(result.asString()).startsWith("GET|q=hello");
    }

    @Test
    void postJsonBody() {
        HttpResult result = HttpClients.post(baseUrl + "/echo")
                .jsonBody(Map.of("sku", "A1"))
                .executeOk();

        assertThat(result.asString()).contains("POST|");
        assertThat(result.asString()).contains("application/json");
        assertThat(result.asString()).contains("\"sku\"");
    }

    @Test
    void postFormBody() {
        String body = HttpClients.post(baseUrl + "/echo")
                .formBody(Map.of("username", "admin", "password", "123"))
                .executeOk()
                .asString();

        assertThat(body).contains("application/x-www-form-urlencoded");
        assertThat(body).contains("username=admin");
    }

    @Test
    void postMultipart() {
        String body = HttpClients.post(baseUrl + "/echo")
                .multipart()
                .text("biz", "import")
                .file("file", "a.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8))
                .done()
                .executeOk()
                .asString();

        assertThat(body).contains("multipart/form-data");
        assertThat(body).contains("biz");
        assertThat(body).contains("hello");
    }

    @Test
    void patchAndDeleteSupported() {
        assertThat(HttpClients.patch(baseUrl + "/echo").jsonBody("{}").executeOk().asString())
                .startsWith("PATCH|");
        assertThat(HttpClients.delete(baseUrl + "/echo").executeOk().asString())
                .startsWith("DELETE|");
    }

    @Test
    void asJsonDeserializes() {
        DemoDto dto = HttpClients.get(baseUrl + "/json").executeOk().asJson(DemoDto.class);
        assertThat(dto.name).isEqualTo("omni");
        assertThat(dto.count).isEqualTo(2);
    }

    @Test
    void requireSuccessThrowsOn5xx() {
        HttpResult result = HttpClients.get(baseUrl + "/fail").execute();
        assertThat(result.isSuccessful()).isFalse();
        org.junit.jupiter.api.Assertions.assertThrows(
                com.omni.scaffolding.common.exception.BusinessException.class,
                result::requireSuccess);
    }

    public static class DemoDto {
        public String name;
        public int count;
    }
}
