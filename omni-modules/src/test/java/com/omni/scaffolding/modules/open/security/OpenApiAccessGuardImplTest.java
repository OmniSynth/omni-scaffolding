package com.omni.scaffolding.modules.open.security;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.common.util.DigestUtils;
import com.omni.scaffolding.infra.ratelimit.RedisRateLimiter;
import com.omni.scaffolding.modules.open.dto.endpoint.OpenEndpointView;
import com.omni.scaffolding.modules.open.entity.OpenApiClient;
import com.omni.scaffolding.modules.open.mapper.OpenApiClientQueryMapper;
import com.omni.scaffolding.modules.open.mapper.OpenApiEndpointQueryMapper;
import com.omni.scaffolding.modules.open.repository.OpenApiClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenApiAccessGuardImplTest {

    @Mock
    private OpenApiClientRepository clientRepository;
    @Mock
    private OpenApiClientQueryMapper clientQueryMapper;
    @Mock
    private OpenApiEndpointQueryMapper endpointQueryMapper;
    @Mock
    private RedisRateLimiter redisRateLimiter;

    @InjectMocks
    private OpenApiAccessGuardImpl guard;

    private OpenApiClient client;

    @BeforeEach
    void setUp() {
        client = new OpenApiClient();
        client.setId(10L);
        client.setName("demo-partner");
        client.setAccessKey("ak_test");
        client.setStatus(true);
        client.setApiKeyHash(DigestUtils.sha256Hex("oak_secret"));
        client.setQpsLimit(null);
        client.setDailyLimit(null);
    }

    @Test
    void authenticateSuccess() {
        when(clientRepository.findByApiKeyHashAndDeleted(DigestUtils.sha256Hex("oak_secret"), 0))
                .thenReturn(Optional.of(client));
        when(clientQueryMapper.listIpsByClientId(10L)).thenReturn(List.of());
        OpenEndpointView ep = new OpenEndpointView();
        ep.setHttpMethod("GET");
        ep.setPathPattern("/api/open/demo/ping");
        when(endpointQueryMapper.listEnabledByClientId(10L)).thenReturn(List.of(ep));

        var principal = guard.authenticate("oak_secret", "127.0.0.1", "GET", "/api/open/demo/ping");
        assertThat(principal.getClientId()).isEqualTo(10L);
        assertThat(principal.getName()).isEqualTo("demo-partner");
    }

    @Test
    void rejectMissingKey() {
        assertThatThrownBy(() -> guard.authenticate(" ", "127.0.0.1", "GET", "/api/open/demo/ping"))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    void rejectIpNotInWhitelist() {
        when(clientRepository.findByApiKeyHashAndDeleted(DigestUtils.sha256Hex("oak_secret"), 0))
                .thenReturn(Optional.of(client));
        when(clientQueryMapper.listIpsByClientId(10L)).thenReturn(List.of("10.0.0.1"));

        assertThatThrownBy(() -> guard.authenticate("oak_secret", "127.0.0.1", "GET", "/api/open/demo/ping"))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void rejectUnauthorizedPath() {
        when(clientRepository.findByApiKeyHashAndDeleted(DigestUtils.sha256Hex("oak_secret"), 0))
                .thenReturn(Optional.of(client));
        when(clientQueryMapper.listIpsByClientId(10L)).thenReturn(List.of());
        when(endpointQueryMapper.listEnabledByClientId(10L)).thenReturn(List.of());

        assertThatThrownBy(() -> guard.authenticate("oak_secret", "127.0.0.1", "GET", "/api/open/demo/ping"))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void rejectWhenQpsExceeded() {
        client.setQpsLimit(5);
        when(clientRepository.findByApiKeyHashAndDeleted(DigestUtils.sha256Hex("oak_secret"), 0))
                .thenReturn(Optional.of(client));
        when(clientQueryMapper.listIpsByClientId(10L)).thenReturn(List.of());
        OpenEndpointView ep = new OpenEndpointView();
        ep.setHttpMethod("*");
        ep.setPathPattern("/api/open/demo/**");
        when(endpointQueryMapper.listEnabledByClientId(10L)).thenReturn(List.of(ep));
        when(redisRateLimiter.tryAcquire(anyString(), eq(5), anyInt())).thenReturn(false);

        assertThatThrownBy(() -> guard.authenticate("oak_secret", "127.0.0.1", "GET", "/api/open/demo/ping"))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.TOO_MANY_REQUESTS);
    }
}
