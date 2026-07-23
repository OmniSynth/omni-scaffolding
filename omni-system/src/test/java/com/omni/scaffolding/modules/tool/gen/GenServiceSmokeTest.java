package com.omni.scaffolding.modules.tool.gen;

import com.omni.scaffolding.modules.ops.service.MysqlOpsService;
import com.omni.scaffolding.modules.tool.gen.dto.GenColumnConfig;
import com.omni.scaffolding.modules.tool.gen.dto.GenFileView;
import com.omni.scaffolding.modules.tool.gen.dto.GenTableConfig;
import com.omni.scaffolding.modules.tool.gen.service.GenService;
import com.omni.scaffolding.modules.tool.gen.support.GenTemplateEngine;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * 不连库：用手写配置走通预览 / ZIP，并核对 BETWEEN 查询产物。
 */
class GenServiceSmokeTest {

    @Test
    void previewAndZipContainBetweenQuery() throws Exception {
        GenService service = new GenService(mock(MysqlOpsService.class), new GenTemplateEngine());
        GenTableConfig cfg = sampleConfig();

        List<GenFileView> files = service.preview(cfg);
        assertFalse(files.isEmpty());
        Map<String, String> byPath = files.stream()
                .collect(Collectors.toMap(GenFileView::getPath, GenFileView::getContent, (a, b) -> a));

        String xml = byPath.entrySet().stream()
                .filter(e -> e.getKey().endsWith("QueryMapper.xml"))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow();
        assertTrue(xml.contains("created_at &gt;= #{createdAtFrom}") || xml.contains("created_at >= #{createdAtFrom}")
                || xml.contains("AND created_at &gt;= #{createdAtFrom}"));
        assertTrue(xml.contains("createdAtFrom"));
        assertTrue(xml.contains("createdAtTo"));
        assertTrue(xml.contains("title LIKE CONCAT"));

        String vue = byPath.entrySet().stream()
                .filter(e -> e.getKey().endsWith("index.vue"))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow();
        assertTrue(vue.contains("datetimerange"));
        assertTrue(vue.contains("createdAtRange"));
        assertTrue(vue.contains("createdAtFrom"));

        String mapperJava = byPath.entrySet().stream()
                .filter(e -> e.getKey().endsWith("QueryMapper.java"))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow();
        assertTrue(mapperJava.contains("createdAtFrom"));
        assertTrue(mapperJava.contains("createdAtTo"));

        String viewJava = byPath.entrySet().stream()
                .filter(e -> e.getKey().endsWith("NoticeView.java"))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow();
        assertTrue(viewJava.contains("@DictText(\"sys_normal_disable\")"));

        String apiTs = byPath.entrySet().stream()
                .filter(e -> e.getKey().endsWith("/notice.ts"))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow();
        assertTrue(apiTs.contains("statusText?: string"));
        assertTrue(vue.contains("row.statusText ?? row.status"));

        byte[] zip = service.downloadZip(cfg);
        assertNotNull(zip);
        assertTrue(zip.length > 100);
        try (ZipInputStream zis = new ZipInputStream(new java.io.ByteArrayInputStream(zip))) {
            assertNotNull(zis.getNextEntry());
        }
    }

    private static GenTableConfig sampleConfig() {
        GenTableConfig cfg = new GenTableConfig();
        cfg.setTableName("sys_notice");
        cfg.setTableComment("通知公告");
        cfg.setModuleName("demo");
        cfg.setBusinessName("notice");
        cfg.setClassName("SysNotice");
        cfg.setFunctionName("通知公告");
        cfg.setPackageName("com.omni.scaffolding.modules.demo");
        cfg.setPermissionPrefix("demo:notice");
        cfg.setAuthor("omni");
        cfg.setMenuParentId(200L);
        cfg.setMenuIdStart(5100L);
        cfg.setMenuSort(10);

        cfg.setColumns(List.of(
                col("id", "bigint", "主键", "id", "Long", true, false, false, true, false, false, "NONE", false, false),
                col("title", "varchar(128)", "标题", "title", "String", false, false, false, true, true, true, "LIKE", false, false),
                col("status", "tinyint(1)", "状态", "status", "Boolean", false, false, false, true, true, true, "EQ", false, false),
                col("created_at", "datetime(3)", "创建时间", "createdAt", "Instant", false, false, false, true, false, false, "BETWEEN", true, false),
                col("updated_at", "datetime(3)", "更新时间", "updatedAt", "Instant", false, false, false, false, false, false, "NONE", true, false),
                col("version", "bigint", "版本", "version", "Long", false, false, false, false, false, false, "NONE", true, false),
                col("deleted", "int", "删除标记", "deleted", "Integer", false, false, false, false, false, false, "NONE", false, true)
        ));
        cfg.getColumns().stream()
                .filter(column -> "status".equals(column.getJavaField()))
                .findFirst()
                .orElseThrow()
                .setDictType("sys_normal_disable");
        return cfg;
    }

    private static GenColumnConfig col(String columnName,
                                       String columnType,
                                       String comment,
                                       String javaField,
                                       String javaType,
                                       boolean pk,
                                       boolean increment,
                                       boolean nullable,
                                       boolean list,
                                       boolean form,
                                       boolean required,
                                       String queryType,
                                       boolean audit,
                                       boolean logicDelete) {
        GenColumnConfig c = new GenColumnConfig();
        c.setColumnName(columnName);
        c.setColumnType(columnType);
        c.setColumnComment(comment);
        c.setJavaField(javaField);
        c.setJavaType(javaType);
        c.setPk(pk);
        c.setIncrement(increment);
        c.setNullable(nullable);
        c.setList(list);
        c.setForm(form);
        c.setRequired(required);
        c.setQueryType(queryType);
        c.setAudit(audit);
        c.setLogicDelete(logicDelete);
        return c;
    }
}
