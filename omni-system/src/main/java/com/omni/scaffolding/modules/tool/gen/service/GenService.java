package com.omni.scaffolding.modules.tool.gen.service;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.modules.ops.dto.MysqlColumnView;
import com.omni.scaffolding.modules.ops.dto.MysqlTableDetailView;
import com.omni.scaffolding.modules.ops.dto.MysqlTableView;
import com.omni.scaffolding.modules.ops.service.MysqlOpsService;
import com.omni.scaffolding.modules.tool.gen.dto.GenColumnConfig;
import com.omni.scaffolding.modules.tool.gen.dto.GenFileView;
import com.omni.scaffolding.modules.tool.gen.dto.GenTableConfig;
import com.omni.scaffolding.modules.tool.gen.support.GenNaming;
import com.omni.scaffolding.modules.tool.gen.support.GenTemplateEngine;
import com.omni.scaffolding.modules.tool.gen.support.GenTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 在线 CRUD 代码生成：表元数据 → 默认配置 → Freemarker 预览 / ZIP。
 */
@Service
@Profile("!test")
@RequiredArgsConstructor
public class GenService {

    private final MysqlOpsService mysqlOpsService;
    private final GenTemplateEngine templateEngine;

    /**
     * 可生成的业务表列表。
     *
     * @param keyword 可选关键字
     * @param page    页码
     * @param size    每页条数
     * @return 分页表列表
     */
    public PageResult<MysqlTableView> listTables(String keyword, Long page, Long size) {
        return mysqlOpsService.listTables(keyword, page, size);
    }

    /**
     * 按表构建默认生成配置。
     *
     * @param tableName 表名
     * @return 默认配置
     */
    public GenTableConfig defaultConfig(String tableName) {
        MysqlTableDetailView detail = mysqlOpsService.tableDetail(tableName);
        MysqlTableView table = detail.getTable();
        String name = table != null ? table.getName() : tableName;

        GenTableConfig cfg = new GenTableConfig();
        cfg.setTableName(name);
        cfg.setTableComment(table != null && StringUtils.hasText(table.getComment()) ? table.getComment() : name);
        cfg.setModuleName("demo");
        cfg.setBusinessName(GenNaming.toBusinessName(name));
        cfg.setClassName(GenNaming.toClassName(name));
        cfg.setFunctionName(cfg.getTableComment());
        cfg.setPackageName("com.omni.scaffolding.modules.demo");
        String biz = cfg.getBusinessName();
        cfg.setPermissionPrefix("demo:" + biz);
        cfg.setAuthor("omni");
        cfg.setMenuParentId(200L);
        cfg.setMenuIdStart(5000L);
        cfg.setMenuSort(99);

        List<GenColumnConfig> columns = new ArrayList<>();
        boolean hasDeleted = false;
        boolean extendsAudit = false;
        String pkField = "id";
        String pkJavaType = "Long";
        String pkColumn = "id";

        for (MysqlColumnView col : detail.getColumns()) {
            GenColumnConfig c = new GenColumnConfig();
            c.setColumnName(col.getName());
            c.setColumnType(col.getType());
            c.setColumnComment(StringUtils.hasText(col.getComment()) ? col.getComment() : col.getName());
            c.setJavaField(GenNaming.toCamel(col.getName(), false));
            c.setJavaType(GenTypeMapper.toJavaType(col.getType()));
            boolean pk = "PRI".equalsIgnoreCase(col.getColumnKey());
            c.setPk(pk);
            c.setIncrement(col.getExtra() != null && col.getExtra().toLowerCase(Locale.ROOT).contains("auto_increment"));
            c.setNullable(!"NO".equalsIgnoreCase(col.getNullable()));
            c.setAudit(GenTypeMapper.isAudit(col.getName()));
            c.setLogicDelete(GenTypeMapper.isLogicDelete(col.getName()));
            c.setQueryType(GenTypeMapper.defaultQueryType(col.getName(), col.getType(), pk));
            c.setList(!c.isLogicDelete() && !c.isAudit());
            c.setForm(!pk && !c.isAudit() && !c.isLogicDelete() && !c.isIncrement());
            c.setRequired(c.isForm() && !c.isNullable());
            if (c.isLogicDelete()) {
                hasDeleted = true;
                c.setList(false);
                c.setForm(false);
                c.setQueryType("NONE");
            }
            if (c.isAudit()) {
                extendsAudit = true;
                c.setList("created_at".equalsIgnoreCase(col.getName()) || "updated_at".equalsIgnoreCase(col.getName()));
                c.setForm(false);
                c.setQueryType("NONE");
            }
            if (pk) {
                pkField = c.getJavaField();
                pkJavaType = c.getJavaType();
                pkColumn = c.getColumnName();
                c.setList(true);
                c.setForm(false);
            }
            columns.add(c);
        }
        cfg.setColumns(columns);
        cfg.setHasDeleted(hasDeleted);
        cfg.setExtendsAudit(extendsAudit);
        cfg.setPkField(pkField);
        cfg.setPkJavaType(pkJavaType);
        cfg.setPkColumn(pkColumn);
        return cfg;
    }

    /**
     * 预览全部生成文件。
     *
     * @param config 表配置
     * @return 文件列表
     */
    public List<GenFileView> preview(GenTableConfig config) {
        return renderAll(normalize(config)).entrySet().stream()
                .map(e -> new GenFileView(e.getKey(), e.getValue()))
                .toList();
    }

    /**
     * 打包 ZIP。
     *
     * @param config 表配置
     * @return zip 字节
     */
    public byte[] downloadZip(GenTableConfig config) {
        Map<String, String> files = renderAll(normalize(config));
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Map.Entry<String, String> e : files.entrySet()) {
                zos.putNextEntry(new ZipEntry(e.getKey()));
                zos.write(e.getValue().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                zos.closeEntry();
            }
            zos.finish();
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "打包 ZIP 失败: " + ex.getMessage());
        }
    }

    private GenTableConfig normalize(GenTableConfig config) {
        if (config == null || !StringUtils.hasText(config.getTableName())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "表名不能为空");
        }
        if (config.getColumns() == null || config.getColumns().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "列配置不能为空");
        }
        config.getColumns().forEach(column ->
                column.setDictType(StringUtils.hasText(column.getDictType()) ? column.getDictType().trim() : null));
        config.getColumns().stream()
                .map(GenColumnConfig::getDictType)
                .filter(StringUtils::hasText)
                .filter(type -> !type.matches("[A-Za-z0-9_.:-]+"))
                .findFirst()
                .ifPresent(type -> {
                    throw new BusinessException(ErrorCode.BAD_REQUEST, "字典类型编码包含非法字符: " + type);
                });
        boolean hasDeleted = config.getColumns().stream().anyMatch(GenColumnConfig::isLogicDelete);
        boolean extendsAudit = config.getColumns().stream().anyMatch(GenColumnConfig::isAudit);
        config.setHasDeleted(hasDeleted);
        config.setExtendsAudit(extendsAudit);
        config.getColumns().stream().filter(GenColumnConfig::isPk).findFirst().ifPresent(pk -> {
            config.setPkField(pk.getJavaField());
            config.setPkJavaType(pk.getJavaType());
            config.setPkColumn(pk.getColumnName());
        });
        if (!StringUtils.hasText(config.getPackageName())) {
            config.setPackageName("com.omni.scaffolding.modules." + config.getModuleName());
        }
        return config;
    }

    private Map<String, String> renderAll(GenTableConfig config) {
        Map<String, Object> model = buildModel(config);
        String pkgPath = config.getPackageName().replace('.', '/');
        String module = config.getModuleName();
        String className = config.getClassName();
        String biz = config.getBusinessName();
        String functionCamel = GenNaming.toCamel(biz, true);

        Map<String, String> files = new LinkedHashMap<>();
        files.put("java/" + pkgPath + "/entity/" + className + ".java",
                templateEngine.render("entity.java.ftl", model));
        files.put("java/" + pkgPath + "/repository/" + className + "Repository.java",
                templateEngine.render("repository.java.ftl", model));
        files.put("java/" + pkgPath + "/mapper/" + className + "QueryMapper.java",
                templateEngine.render("mapper.java.ftl", model));
        files.put("resources/mapper/" + module + "/" + className + "QueryMapper.xml",
                templateEngine.render("mapper.xml.ftl", model));
        files.put("java/" + pkgPath + "/dto/" + functionCamel + "View.java",
                templateEngine.render("view.java.ftl", model));
        files.put("java/" + pkgPath + "/dto/" + functionCamel + "SaveRequest.java",
                templateEngine.render("saveRequest.java.ftl", model));
        files.put("java/" + pkgPath + "/service/" + functionCamel + "Service.java",
                templateEngine.render("service.java.ftl", model));
        files.put("java/" + pkgPath + "/controller/" + functionCamel + "Controller.java",
                templateEngine.render("controller.java.ftl", model));
        files.put("omni-web/src/api/" + module + "/" + biz + ".ts",
                templateEngine.render("api.ts.ftl", model));
        files.put("omni-web/src/views/" + module + "/" + biz + "/index.vue",
                templateEngine.render("index.vue.ftl", model));
        files.put("sql/menu.sql", templateEngine.render("menu.sql.ftl", model));
        files.put("README_GEN.md", templateEngine.render("README_GEN.md.ftl", model));
        return files;
    }

    private Map<String, Object> buildModel(GenTableConfig config) {
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("cfg", config);
        model.put("functionCamel", GenNaming.toCamel(config.getBusinessName(), true));
        model.put("functionCamelLower", GenNaming.toCamel(config.getBusinessName(), false));
        model.put("apiPath", "/api/" + config.getModuleName() + "/" + config.getBusinessName() + "s");
        model.put("perm", config.getPermissionPrefix());
        model.put("queryColumns", config.getColumns().stream()
                .filter(c -> c.getQueryType() != null && !"NONE".equalsIgnoreCase(c.getQueryType()))
                .toList());
        model.put("listColumns", config.getColumns().stream().filter(GenColumnConfig::isList).toList());
        model.put("formColumns", config.getColumns().stream().filter(GenColumnConfig::isForm).toList());
        model.put("viewColumns", config.getColumns().stream().filter(c -> !c.isLogicDelete()).toList());
        List<GenColumnConfig> dictColumns = config.getColumns().stream()
                .filter(c -> StringUtils.hasText(c.getDictType()))
                .toList();
        model.put("dictColumns", dictColumns);
        model.put("hasDictColumns", !dictColumns.isEmpty());
        // 继承审计基类时，实体不再重复声明 created_at/updated_at/version
        List<GenColumnConfig> entityCols = new ArrayList<>();
        for (GenColumnConfig c : config.getColumns()) {
            if (config.isExtendsAudit() && c.isAudit()) {
                continue;
            }
            entityCols.add(c);
        }
        model.put("entityColumns", entityCols);
        boolean needBigDecimal = config.getColumns().stream().anyMatch(c -> "BigDecimal".equals(c.getJavaType()));
        boolean needInstant = config.getColumns().stream().anyMatch(c -> "Instant".equals(c.getJavaType()));
        model.put("needBigDecimal", needBigDecimal);
        model.put("needInstant", needInstant);
        model.put("menuId", config.getMenuIdStart());
        model.put("menuQueryId", config.getMenuIdStart() + 1);
        model.put("menuAddId", config.getMenuIdStart() + 2);
        model.put("menuEditId", config.getMenuIdStart() + 3);
        model.put("menuRemoveId", config.getMenuIdStart() + 4);
        model.put("componentPath", config.getModuleName() + "/" + config.getBusinessName() + "/index");
        model.put("routePath", config.getModuleName() + "/" + config.getBusinessName());
        return model;
    }
}
