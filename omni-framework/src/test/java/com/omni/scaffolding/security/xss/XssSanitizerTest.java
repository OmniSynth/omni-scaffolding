package com.omni.scaffolding.security.xss;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XssSanitizerTest {

    @Test
    void strip_removesScriptAndEventHandlers() {
        String dirty = "hello<script>alert(1)</script><img src=x onerror=alert(1)>world";
        String clean = XssSanitizer.clean(dirty, XssMode.STRIP);
        assertThat(clean).doesNotContain("<script>");
        assertThat(clean).doesNotContain("onerror");
        assertThat(clean).contains("hello");
        assertThat(clean).contains("world");
    }

    @Test
    void strip_neutralizesJavascriptProtocol() {
        String dirty = "<a href=\"javascript:alert(1)\">x</a>";
        String clean = XssSanitizer.clean(dirty, XssMode.STRIP);
        assertThat(clean.toLowerCase()).doesNotContain("javascript:");
    }

    @Test
    void escape_encodesHtmlSpecialChars() {
        String clean = XssSanitizer.clean("<b>&\"'</b>", XssMode.ESCAPE);
        assertThat(clean).contains("&lt;").contains("&gt;").contains("&amp;");
    }

    @Test
    void nullAndEmpty_passthrough() {
        assertThat(XssSanitizer.clean(null, XssMode.STRIP)).isNull();
        assertThat(XssSanitizer.clean("", XssMode.STRIP)).isEmpty();
    }
}
