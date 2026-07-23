package com.omni.scaffolding.common.excel;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 将 {@link ExcelUtils} 写出结果写入 HTTP 下载响应。
 */
public final class ExcelExportHelper {

    private ExcelExportHelper() {
    }

    /**
     * 写出 xlsx 附件。
     *
     * @param response HTTP 响应
     * @param filename 下载文件名（含扩展名，如 {@code 用户数据.xlsx}）
     * @param type     导出行类型
     * @param rows     数据行
     */
    public static <T> void write(HttpServletResponse response, String filename, Class<T> type, List<T> rows) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
            ExcelUtils.write(response.getOutputStream(), type, rows);
            response.flushBuffer();
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "导出失败: " + ex.getMessage());
        }
    }
}
