package com.omni.scaffolding.common.desensitize;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DesensitizeUtilsTest {

    @Test
    void maskName_variants() {
        assertThat(DesensitizeUtils.desensitize("张", DesensitizeType.NAME)).isEqualTo("*");
        assertThat(DesensitizeUtils.desensitize("张三", DesensitizeType.NAME)).isEqualTo("张*");
        assertThat(DesensitizeUtils.desensitize("张三丰", DesensitizeType.NAME)).isEqualTo("张*丰");
        assertThat(DesensitizeUtils.desensitize("欧阳修远", DesensitizeType.NAME)).isEqualTo("欧**远");
    }

    @Test
    void maskMobile_keepsPrefixAndSuffix() {
        assertThat(DesensitizeUtils.desensitize("13812345678", DesensitizeType.MOBILE))
                .isEqualTo("138****5678");
    }

    @Test
    void maskIdCard_keepsEnds() {
        assertThat(DesensitizeUtils.desensitize("110101199001011234", DesensitizeType.ID_CARD))
                .isEqualTo("1101**********1234");
    }

    @Test
    void maskEmail_keepsFirstLocalAndDomain() {
        assertThat(DesensitizeUtils.desensitize("admin@omni.local", DesensitizeType.EMAIL))
                .isEqualTo("a****@omni.local");
    }

    @Test
    void maskBankCard_keepsLast4() {
        assertThat(DesensitizeUtils.desensitize("6222021234567890", DesensitizeType.BANK_CARD))
                .isEqualTo("************7890");
    }

    @Test
    void maskAddress_keepsPrefix() {
        String address = "北京市朝阳区建国路1号";
        String masked = DesensitizeUtils.desensitize(address, DesensitizeType.ADDRESS);
        assertThat(masked).startsWith(address.substring(0, 6));
        assertThat(masked.substring(6)).matches("\\*+");
        assertThat(masked).hasSize(address.length());
    }

    @Test
    void maskCustom_usesKeepCounts() {
        assertThat(DesensitizeUtils.desensitize("ABCDEFGH", DesensitizeType.CUSTOM, 2, 2, '#'))
                .isEqualTo("AB####GH");
    }

    @Test
    void jackson_serializesWithAnnotation() throws Exception {
        DemoVo vo = new DemoVo();
        vo.setRealName("张三丰");
        vo.setMobile("13812345678");
        vo.setIdCard("110101199001011234");
        vo.setEmail("zhang@omni.local");

        String json = new ObjectMapper().writeValueAsString(vo);
        assertThat(json).contains("\"realName\":\"张*丰\"");
        assertThat(json).contains("\"mobile\":\"138****5678\"");
        assertThat(json).contains("\"idCard\":\"1101**********1234\"");
        assertThat(json).contains("\"email\":\"z****@omni.local\"");
    }

    @Test
    void jackson_skipsWhenContextIgnored() throws Exception {
        DemoVo vo = new DemoVo();
        vo.setMobile("13812345678");

        String json = DesensitizeContext.runWithout(() -> {
            try {
                return new ObjectMapper().writeValueAsString(vo);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        assertThat(json).contains("\"mobile\":\"13812345678\"");
    }

    @Data
    static class DemoVo {
        @Desensitize(type = DesensitizeType.NAME)
        private String realName;

        @Desensitize(type = DesensitizeType.MOBILE)
        private String mobile;

        @Desensitize(type = DesensitizeType.ID_CARD)
        private String idCard;

        @Desensitize(type = DesensitizeType.EMAIL)
        private String email;
    }
}
