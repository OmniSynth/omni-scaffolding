package com.omni.scaffolding.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IpUtilsTest {

    @Test
    void prefersForwardedWhenPeerIsLoopback() {
        assertEquals("192.168.1.88",
                IpUtils.resolveClientIp("192.168.1.88, 127.0.0.1", null, "127.0.0.1"));
        assertEquals("10.0.0.9",
                IpUtils.resolveClientIp(null, "10.0.0.9", "::1"));
    }

    @Test
    void prefersForwardedWhenPeerIsPrivateProxy() {
        assertEquals("203.0.113.50",
                IpUtils.resolveClientIp("203.0.113.50", null, "192.168.1.2"));
    }

    @Test
    void ignoresSpoofedForwardedWhenPeerIsExternal() {
        assertEquals("203.0.113.10",
                IpUtils.resolveClientIp("1.2.3.4", "1.2.3.4", "203.0.113.10"));
    }

    @Test
    void normalizesIpv6MappedAndLoopback() {
        assertEquals("192.168.0.5", IpUtils.normalize("::ffff:192.168.0.5"));
        assertEquals("127.0.0.1", IpUtils.normalize("::1"));
        assertTrue(IpUtils.isTrustedProxyPeer("127.0.0.1"));
        assertTrue(IpUtils.isTrustedProxyPeer("::ffff:127.0.0.1"));
        assertTrue(IpUtils.isTrustedProxyPeer("10.1.2.3"));
        assertTrue(IpUtils.isTrustedProxyPeer("172.16.0.1"));
        assertFalse(IpUtils.isTrustedProxyPeer("172.32.0.1"));
        assertFalse(IpUtils.isTrustedProxyPeer("8.8.8.8"));
    }

    @Test
    void fallsBackToRemoteAddr() {
        assertEquals("192.168.1.20",
                IpUtils.resolveClientIp(null, null, "192.168.1.20"));
        assertEquals("192.168.1.20",
                IpUtils.resolveClientIp("unknown", "unknown", "::ffff:192.168.1.20"));
    }
}
