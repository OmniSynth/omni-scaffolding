package com.omni.scaffolding.modules.ops.service;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.modules.ops.dto.MysqlColumnView;
import com.omni.scaffolding.modules.ops.dto.MysqlCreateIndexRequest;
import com.omni.scaffolding.modules.ops.dto.MysqlDropIndexRequest;
import com.omni.scaffolding.modules.ops.dto.MysqlIndexView;
import com.omni.scaffolding.modules.ops.dto.MysqlOverviewView;
import com.omni.scaffolding.modules.ops.dto.MysqlProcessView;
import com.omni.scaffolding.modules.ops.dto.MysqlTableDetailView;
import com.omni.scaffolding.modules.ops.dto.MysqlTableView;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * MySQL 运维：库概览、表结构、索引增删、ANALYZE、进程列表。
 *
 * <p>仅操作当前数据源库；标识符白名单校验后拼接 DDL，不提供任意 SQL。
 */
@Service
@Profile("!test")
@RequiredArgsConstructor
public class MysqlOpsService {

    private static final Pattern IDENTIFIER = Pattern.compile("^[A-Za-z0-9_]{1,64}$");
    private static final int MAX_INDEX_COLUMNS = 16;

    private final JdbcTemplate jdbcTemplate;

    /**
     * 当前库概览。
     *
     * @return 版本、表数量、空间占用等
     */
    public MysqlOverviewView overview() {
        String schema = currentSchema();
        MysqlOverviewView view = new MysqlOverviewView();
        view.setVersion(jdbcTemplate.queryForObject("SELECT VERSION()", String.class));
        view.setSchema(schema);

        Map<String, Object> agg = jdbcTemplate.queryForMap("""
                SELECT COUNT(*) AS table_count,
                       COALESCE(SUM(TABLE_ROWS), 0) AS total_rows,
                       COALESCE(SUM(DATA_LENGTH), 0) AS data_length,
                       COALESCE(SUM(INDEX_LENGTH), 0) AS index_length,
                       COALESCE(SUM(DATA_FREE), 0) AS data_free
                FROM information_schema.TABLES
                WHERE TABLE_SCHEMA = ? AND TABLE_TYPE = 'BASE TABLE'
                """, schema);
        view.setTableCount(asLong(agg.get("table_count")));
        view.setTotalRows(asLong(agg.get("total_rows")));
        view.setDataLength(asLong(agg.get("data_length")));
        view.setIndexLength(asLong(agg.get("index_length")));
        view.setDataFree(asLong(agg.get("data_free")));

        Map<String, Object> charset = jdbcTemplate.queryForMap("""
                SELECT DEFAULT_CHARACTER_SET_NAME AS cs, DEFAULT_COLLATION_NAME AS coll
                FROM information_schema.SCHEMATA
                WHERE SCHEMA_NAME = ?
                """, schema);
        view.setCharacterSet(asString(charset.get("cs")));
        view.setCollation(asString(charset.get("coll")));
        return view;
    }

    /**
     * 表列表。
     *
     * @param keyword 可选，表名模糊匹配
     * @return 表列表
     */
    public List<MysqlTableView> listTables(String keyword) {
        String schema = currentSchema();
        String kw = StringUtils.hasText(keyword) ? keyword.trim() : null;
        if (kw != null && !IDENTIFIER.matcher(kw.replace("%", "").replace("*", "")).matches()
                && !kw.chars().allMatch(c -> Character.isLetterOrDigit(c) || c == '_' || c == '%' || c == '*')) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "关键字包含非法字符");
        }
        String like = kw == null ? null : kw.replace('*', '%');

        String sql = """
                SELECT TABLE_NAME, ENGINE, TABLE_ROWS, DATA_LENGTH, INDEX_LENGTH, DATA_FREE,
                       TABLE_COLLATION, TABLE_COMMENT, CREATE_TIME, UPDATE_TIME
                FROM information_schema.TABLES
                WHERE TABLE_SCHEMA = ? AND TABLE_TYPE = 'BASE TABLE'
                """;
        List<Object> args = new ArrayList<>();
        args.add(schema);
        if (like != null) {
            sql += " AND TABLE_NAME LIKE ?";
            args.add(like);
        }
        sql += " ORDER BY TABLE_NAME";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            MysqlTableView t = new MysqlTableView();
            t.setName(rs.getString("TABLE_NAME"));
            t.setEngine(rs.getString("ENGINE"));
            t.setTableRows(asLong(rs.getObject("TABLE_ROWS")));
            t.setDataLength(asLong(rs.getObject("DATA_LENGTH")));
            t.setIndexLength(asLong(rs.getObject("INDEX_LENGTH")));
            t.setDataFree(asLong(rs.getObject("DATA_FREE")));
            t.setCollation(rs.getString("TABLE_COLLATION"));
            t.setComment(rs.getString("TABLE_COMMENT"));
            t.setCreateTime(formatTs(rs.getTimestamp("CREATE_TIME")));
            t.setUpdateTime(formatTs(rs.getTimestamp("UPDATE_TIME")));
            return t;
        }, args.toArray());
    }

    /**
     * 表详情（列 / 索引 / DDL）。
     *
     * @param table 表名
     * @return 表详情
     */
    public MysqlTableDetailView tableDetail(String table) {
        String tableName = requireIdentifier(table, "表名");
        String schema = currentSchema();
        ensureTableExists(schema, tableName);

        MysqlTableDetailView detail = new MysqlTableDetailView();
        detail.setTable(listTables(tableName).stream()
                .filter(t -> tableName.equalsIgnoreCase(t.getName()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "表不存在: " + tableName)));

        detail.setColumns(jdbcTemplate.query("""
                SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_KEY, COLUMN_DEFAULT, EXTRA,
                       COLUMN_COMMENT, ORDINAL_POSITION
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?
                ORDER BY ORDINAL_POSITION
                """, (rs, rowNum) -> {
            MysqlColumnView c = new MysqlColumnView();
            c.setName(rs.getString("COLUMN_NAME"));
            c.setType(rs.getString("COLUMN_TYPE"));
            c.setNullable(rs.getString("IS_NULLABLE"));
            c.setColumnKey(rs.getString("COLUMN_KEY"));
            c.setDefaultValue(rs.getString("COLUMN_DEFAULT"));
            c.setExtra(rs.getString("EXTRA"));
            c.setComment(rs.getString("COLUMN_COMMENT"));
            c.setOrdinalPosition(asLong(rs.getObject("ORDINAL_POSITION")));
            return c;
        }, schema, tableName));

        detail.setIndexes(loadIndexes(schema, tableName));

        Map<String, Object> create = jdbcTemplate.queryForMap("SHOW CREATE TABLE `" + tableName + "`");
        Object ddl = create.get("Create Table");
        if (ddl == null) {
            ddl = create.values().stream().skip(1).findFirst().orElse("");
        }
        detail.setDdl(Objects.toString(ddl, ""));
        return detail;
    }

    /**
     * 创建索引。
     *
     * @param request 创建索引请求
     * @return 更新后的表详情
     */
    public MysqlTableDetailView createIndex(MysqlCreateIndexRequest request) {
        String table = requireIdentifier(request.getTable(), "表名");
        String indexName = requireIdentifier(request.getName(), "索引名");
        if ("PRIMARY".equalsIgnoreCase(indexName)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能创建名为 PRIMARY 的索引");
        }
        List<String> columns = normalizeColumns(request.getColumns());
        String schema = currentSchema();
        ensureTableExists(schema, table);
        columns = resolveColumnNames(schema, table, columns);

        Integer exists = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND INDEX_NAME = ?
                """, Integer.class, schema, table, indexName);
        if (exists != null && exists > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "索引已存在: " + indexName);
        }

        String colSql = columns.stream().map(c -> "`" + c + "`").collect(Collectors.joining(", "));
        String ddl = (request.isUnique() ? "CREATE UNIQUE INDEX " : "CREATE INDEX ")
                + "`" + indexName + "` ON `" + table + "` (" + colSql + ")";
        try {
            jdbcTemplate.execute(ddl);
        } catch (DataAccessException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "创建索引失败: " + rootMessage(ex));
        }
        return tableDetail(table);
    }

    /**
     * 删除索引。
     *
     * @param request 删除索引请求
     * @return 更新后的表详情
     */
    public MysqlTableDetailView dropIndex(MysqlDropIndexRequest request) {
        String table = requireIdentifier(request.getTable(), "表名");
        String indexName = requireIdentifier(request.getName(), "索引名");
        if ("PRIMARY".equalsIgnoreCase(indexName)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "禁止删除主键索引 PRIMARY");
        }
        String schema = currentSchema();
        ensureTableExists(schema, table);

        Integer exists = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND INDEX_NAME = ?
                """, Integer.class, schema, table, indexName);
        if (exists == null || exists == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "索引不存在: " + indexName);
        }

        String ddl = "DROP INDEX `" + indexName + "` ON `" + table + "`";
        try {
            jdbcTemplate.execute(ddl);
        } catch (DataAccessException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "删除索引失败: " + rootMessage(ex));
        }
        return tableDetail(table);
    }

    /**
     * ANALYZE TABLE。
     *
     * @param table 表名
     * @return 执行结果消息
     */
    public Map<String, Object> analyzeTable(String table) {
        String tableName = requireIdentifier(table, "表名");
        ensureTableExists(currentSchema(), tableName);
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("ANALYZE TABLE `" + tableName + "`");
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("table", tableName);
            result.put("messages", rows);
            return result;
        } catch (DataAccessException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "ANALYZE 失败: " + rootMessage(ex));
        }
    }

    /**
     * 进程列表。
     *
     * @return 连接进程列表
     */
    public List<MysqlProcessView> listProcesses() {
        return jdbcTemplate.query("SHOW FULL PROCESSLIST", (rs, rowNum) -> {
            MysqlProcessView p = new MysqlProcessView();
            p.setId(asLong(rs.getObject("Id")));
            p.setUser(rs.getString("User"));
            p.setHost(rs.getString("Host"));
            p.setDb(rs.getString("db"));
            p.setCommand(rs.getString("Command"));
            p.setTime(asLong(rs.getObject("Time")));
            p.setState(rs.getString("State"));
            p.setInfo(rs.getString("Info"));
            return p;
        });
    }

    /**
     * Kill 连接。
     *
     * @param id 进程 ID
     * @return 执行结果
     */
    public Map<String, Object> killProcess(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "进程 ID 无效");
        }
        try {
            jdbcTemplate.execute("KILL " + id);
        } catch (DataAccessException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Kill 失败: " + rootMessage(ex));
        }
        return Map.of("killed", id);
    }

    /**
     * 从 information_schema 加载指定表的索引定义。
     *
     * @param schema    库名
     * @param tableName 表名
     * @return 索引视图列表
     */
    private List<MysqlIndexView> loadIndexes(String schema, String tableName) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT INDEX_NAME, NON_UNIQUE, INDEX_TYPE, COLUMN_NAME, SEQ_IN_INDEX, CARDINALITY
                FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?
                ORDER BY INDEX_NAME, SEQ_IN_INDEX
                """, schema, tableName);

        Map<String, MysqlIndexView> byName = new LinkedHashMap<>();
        Map<String, List<String>> cols = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String name = asString(row.get("INDEX_NAME"));
            MysqlIndexView idx = byName.computeIfAbsent(name, n -> {
                MysqlIndexView v = new MysqlIndexView();
                v.setName(n);
                v.setUnique(asLong(row.get("NON_UNIQUE")) != null && asLong(row.get("NON_UNIQUE")) == 0L);
                v.setIndexType(asString(row.get("INDEX_TYPE")));
                v.setCardinality(asLong(row.get("CARDINALITY")));
                return v;
            });
            Long card = asLong(row.get("CARDINALITY"));
            if (card != null && (idx.getCardinality() == null || card > idx.getCardinality())) {
                idx.setCardinality(card);
            }
            cols.computeIfAbsent(name, k -> new ArrayList<>()).add(asString(row.get("COLUMN_NAME")));
        }
        for (Map.Entry<String, MysqlIndexView> e : byName.entrySet()) {
            e.getValue().setColumns(String.join(", ", cols.getOrDefault(e.getKey(), List.of())));
        }
        return new ArrayList<>(byName.values());
    }

    /**
     * 获取当前 JDBC 连接所在的数据库名。
     *
     * @return 当前 schema 名
     */
    private String currentSchema() {
        String schema = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        if (!StringUtils.hasText(schema)) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "当前连接未选择数据库");
        }
        return schema;
    }

    /**
     * 校验表在指定 schema 中存在；不存在则 404。
     *
     * @param schema 库名
     * @param table  表名
     */
    private void ensureTableExists(String schema, String table) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM information_schema.TABLES
                WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND TABLE_TYPE = 'BASE TABLE'
                """, Integer.class, schema, table);
        if (count == null || count == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "表不存在: " + table);
        }
    }

    /**
     * 将请求中的列名解析为表内实际列名（大小写不敏感）。
     *
     * @param schema  库名
     * @param table   表名
     * @param columns 请求列名列表
     * @return 实际列名列表
     */
    private List<String> resolveColumnNames(String schema, String table, List<String> columns) {
        Map<String, String> byLower = new LinkedHashMap<>();
        jdbcTemplate.query("""
                SELECT COLUMN_NAME FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?
                """, rs -> {
            String name = rs.getString(1);
            byLower.putIfAbsent(name.toLowerCase(Locale.ROOT), name);
        }, schema, table);
        List<String> resolved = new ArrayList<>();
        for (String col : columns) {
            String actual = byLower.get(col.toLowerCase(Locale.ROOT));
            if (actual == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "列不存在: " + col);
            }
            resolved.add(actual);
        }
        return resolved;
    }

    /**
     * 校验并规范化索引列名（去重、格式、数量上限）。
     *
     * @param columns 原始列名列表
     * @return 规范化后的列名列表
     */
    private List<String> normalizeColumns(List<String> columns) {
        if (columns == null || columns.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "索引列不能为空");
        }
        if (columns.size() > MAX_INDEX_COLUMNS) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "索引列最多 " + MAX_INDEX_COLUMNS + " 个");
        }
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String col : columns) {
            String name = requireIdentifier(col, "列名");
            if (!unique.add(name)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "索引列重复: " + name);
            }
        }
        return new ArrayList<>(unique);
    }

    /**
     * 校验 SQL 标识符合法（字母、数字、下划线，1~64 字符）。
     *
     * @param value 原始值
     * @param label 字段中文名（用于错误提示）
     * @return trim 后的标识符
     */
    private static String requireIdentifier(String value, String label) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, label + "不能为空");
        }
        String trimmed = value.trim();
        if (!IDENTIFIER.matcher(trimmed).matches()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, label + "仅允许字母、数字、下划线，且长度 1~64");
        }
        return trimmed;
    }

    /**
     * 将 JDBC 返回值转为 {@link Long}；无法转换时 {@code null}。
     *
     * @param value 原始值
     * @return 长整型，失败时为 {@code null}
     */
    private static Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * 将 JDBC 返回值转为字符串；{@code null} 保持 {@code null}。
     *
     * @param value 原始值
     * @return 字符串表示
     */
    private static String asString(Object value) {
        return value == null ? null : value.toString();
    }

    /**
     * 将 {@link Timestamp} 格式化为 {@code yyyy-MM-dd HH:mm:ss} 字符串。
     *
     * @param ts 时间戳
     * @return 格式化结果，{@code null} 输入返回 {@code null}
     */
    private static String formatTs(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime().toString().replace('T', ' ');
    }

    /**
     * 取异常链最底层的消息，用于对外错误提示。
     *
     * @param ex 原始异常
     * @return 根因消息
     */
    private static String rootMessage(Throwable ex) {
        Throwable cur = ex;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        String msg = cur.getMessage();
        return StringUtils.hasText(msg) ? msg : ex.getMessage();
    }
}
