package com.omni.scaffolding.system;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omni.scaffolding.support.TestRedisConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
class DataScopeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void adminSeesAllUsers_salesSeesSelfOnly() throws Exception {
        String adminToken = login("admin", "admin123");
        MvcResult adminUsers = mockMvc.perform(get("/api/system/users")
                        .param("page", "1")
                        .param("size", "50")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        JsonNode adminData = objectMapper.readTree(adminUsers.getResponse().getContentAsString()).path("data");
        assertThat(adminData.path("total").asLong()).isGreaterThanOrEqualTo(4);
        assertThat(adminData.path("records").size()).isGreaterThanOrEqualTo(4);

        String salesToken = login("sales1", "admin123");
        MvcResult salesUsers = mockMvc.perform(get("/api/system/users")
                        .param("page", "1")
                        .param("size", "50")
                        .header("Authorization", "Bearer " + salesToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        JsonNode data = objectMapper.readTree(salesUsers.getResponse().getContentAsString()).path("data");
        assertThat(data.path("total").asLong()).isEqualTo(1);
        assertThat(data.path("records").size()).isEqualTo(1);
        assertThat(data.path("records").get(0).path("username").asText()).isEqualTo("sales1");
    }

    @Test
    void rdManagerSeesDeptUsers() throws Exception {
        String token = login("rd_mgr", "admin123");
        MvcResult result = mockMvc.perform(get("/api/system/users")
                        .param("page", "1")
                        .param("size", "50")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        assertThat(data.path("total").asLong()).isEqualTo(2);
        assertThat(data.path("records").findValuesAsText("username")).containsExactlyInAnyOrder("rd_mgr", "rd_dev");
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("accessToken").asText();
    }
}
