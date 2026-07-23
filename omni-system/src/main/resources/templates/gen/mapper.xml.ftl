<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${cfg.packageName}.mapper.${cfg.className}QueryMapper">

    <sql id="SearchWhere">
        WHERE 1 = 1
<#if cfg.hasDeleted>
        AND deleted = 0
</#if>
<#list queryColumns as col>
<#if col.queryType == "LIKE">
        <if test="${col.javaField} != null and ${col.javaField} != ''">
            AND ${col.columnName} LIKE CONCAT('%', ${r"#{"}${col.javaField}${r"}"}, '%')
        </if>
<#elseif col.queryType == "EQ">
        <if test="${col.javaField} != null<#if col.javaType == 'String'> and ${col.javaField} != ''</#if>">
            AND ${col.columnName} = ${r"#{"}${col.javaField}${r"}"}
        </if>
<#elseif col.queryType == "BETWEEN">
        <if test="${col.javaField}From != null">
            AND ${col.columnName} &gt;= ${r"#{"}${col.javaField}From${r"}"}
        </if>
        <if test="${col.javaField}To != null">
            AND ${col.columnName} &lt;= ${r"#{"}${col.javaField}To${r"}"}
        </if>
</#if>
</#list>
    </sql>

    <sql id="BaseColumns">
<#list viewColumns as col>
        ${col.columnName} AS ${col.javaField}<#if col_has_next>,</#if>
</#list>
    </sql>

    <select id="count" resultType="long">
        SELECT COUNT(1)
        FROM ${cfg.tableName}
        <include refid="SearchWhere"/>
    </select>

    <select id="list" resultType="${cfg.packageName}.dto.${functionCamel}View">
        SELECT
        <include refid="BaseColumns"/>
        FROM ${cfg.tableName}
        <include refid="SearchWhere"/>
        ORDER BY ${cfg.pkColumn} DESC
        LIMIT ${r"#{limit}"} OFFSET ${r"#{offset}"}
    </select>

    <select id="findById" resultType="${cfg.packageName}.dto.${functionCamel}View">
        SELECT
        <include refid="BaseColumns"/>
        FROM ${cfg.tableName}
        WHERE ${cfg.pkColumn} = ${r"#{id}"}
<#if cfg.hasDeleted>
          AND deleted = 0
</#if>
    </select>
</mapper>
