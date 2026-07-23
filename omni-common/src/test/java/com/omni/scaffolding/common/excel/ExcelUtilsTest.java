package com.omni.scaffolding.common.excel;

import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExcelUtilsTest {

    @Test
    void writeAndRead_roundTrip_withDropdownAndRequired() {
        DemoRow row = new DemoRow();
        row.setSku("SKU-001");
        row.setName("机械键盘");
        row.setStatus(DemoStatus.ACTIVE);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelUtils.write(out, DemoRow.class, List.of(row));

        List<DemoRow> loaded = ExcelUtils.read(new ByteArrayInputStream(out.toByteArray()), DemoRow.class);
        assertThat(loaded).hasSize(1);
        assertThat(loaded.getFirst().getSku()).isEqualTo("SKU-001");
        assertThat(loaded.getFirst().getName()).isEqualTo("机械键盘");
        assertThat(loaded.getFirst().getStatus()).isEqualTo(DemoStatus.ACTIVE);
    }

    @Test
    void writeTemplate_containsHeaderOnly() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelUtils.writeTemplate(out, DemoRow.class);
        List<DemoRow> loaded = ExcelUtils.read(new ByteArrayInputStream(out.toByteArray()), DemoRow.class);
        assertThat(loaded).isEmpty();
    }

    @ExcelSheet("商品导入")
    @Data
    public static class DemoRow {

        @ExcelColumn(name = "SKU", order = 1, required = true, width = 16, comment = "业务唯一键")
        private String sku;

        @ExcelColumn(name = "名称", order = 2, required = true)
        private String name;

        @ExcelColumn(name = "状态", order = 3, dropdownEnum = DemoStatus.class, required = true)
        private DemoStatus status;
    }

    public enum DemoStatus implements ExcelDropdownLabel {
        ACTIVE("启用"),
        INACTIVE("停用");

        private final String label;

        DemoStatus(String label) {
            this.label = label;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }
}
