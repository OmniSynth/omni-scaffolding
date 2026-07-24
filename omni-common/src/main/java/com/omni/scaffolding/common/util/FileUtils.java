package com.omni.scaffolding.common.util;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

/**
 * 文件工具：继承 Apache Commons {@link org.apache.commons.io.FileUtils}，
 * 并补充路径/文件名处理、可读大小、安全校验等常用能力。
 *
 * <p>可直接通过本类调用父类静态方法，例如 {@code FileUtils.readFileToString(file, UTF_8)}。
 * 对象存储上传请走 {@code FileService}；本类面向本地路径与通用文件操作。
 */
public class FileUtils extends org.apache.commons.io.FileUtils {

    private static final String[] SIZE_UNITS = {"B", "KB", "MB", "GB", "TB"};

    /**
     * 供子类或工具框架实例化；日常请使用静态方法。
     */
    public FileUtils() {
    }

    /**
     * 取文件名（不含路径）。
     *
     * @param path 路径或文件名，可为 null
     * @return 文件名；空白时返回空串
     */
    public static String getName(String path) {
        if (StringUtils.isBlank(path)) {
            return StringUtils.EMPTY;
        }
        return FilenameUtils.getName(path);
    }

    /**
     * 取不含扩展名的主文件名。
     *
     * @param path 路径或文件名
     * @return 主名；空白时返回空串
     */
    public static String getBaseName(String path) {
        if (StringUtils.isBlank(path)) {
            return StringUtils.EMPTY;
        }
        return FilenameUtils.getBaseName(path);
    }

    /**
     * 取扩展名（不含点），小写；无扩展名返回空串。
     *
     * <p>{@code jpeg} 归一为 {@code jpg}。
     *
     * @param path 路径或文件名
     * @return 扩展名
     */
    public static String getExtension(String path) {
        if (StringUtils.isBlank(path)) {
            return StringUtils.EMPTY;
        }
        String ext = FilenameUtils.getExtension(path);
        if (StringUtils.isBlank(ext)) {
            return StringUtils.EMPTY;
        }
        ext = ext.toLowerCase(Locale.ROOT);
        return "jpeg".equals(ext) ? "jpg" : ext;
    }

    /**
     * 清洗文件名：去掉路径分隔符与控制字符，防止路径穿越。
     *
     * @param filename 原始文件名
     * @return 安全文件名；空白时返回 {@code file}
     */
    public static String sanitizeFileName(String filename) {
        String name = getName(filename);
        if (StringUtils.isBlank(name)) {
            return "file";
        }
        StringBuilder sb = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '/' || c == '\\' || c < 32 || c == 127) {
                continue;
            }
            sb.append(c);
        }
        String cleaned = sb.toString().trim();
        if (cleaned.isEmpty() || ".".equals(cleaned) || "..".equals(cleaned)) {
            return "file";
        }
        return cleaned;
    }

    /**
     * 校验相对路径是否安全（禁止绝对路径与 {@code ..} 穿越）。
     *
     * @param relativePath 相对路径
     * @return 安全则为 {@code true}
     */
    public static boolean isSafeRelativePath(String relativePath) {
        if (StringUtils.isBlank(relativePath)) {
            return false;
        }
        String normalized = relativePath.replace('\\', '/').trim();
        if (normalized.startsWith("/") || normalized.contains(":/")) {
            return false;
        }
        for (String segment : normalized.split("/")) {
            if ("..".equals(segment)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 字节数格式化为可读大小，例如 {@code 1.5 MB}。
     *
     * @param bytes 字节数，负数按 0
     * @return 可读字符串
     */
    public static String formatSize(long bytes) {
        long size = Math.max(bytes, 0L);
        if (size < 1024) {
            return size + " B";
        }
        double value = size;
        int unit = 0;
        while (value >= 1024 && unit < SIZE_UNITS.length - 1) {
            value /= 1024;
            unit++;
        }
        return String.format(Locale.ROOT, "%.1f %s", value, SIZE_UNITS[unit]);
    }

    /**
     * 确保目录存在（含多级）。
     *
     * @param dir 目录
     * @return 目录 Path
     */
    public static Path ensureDir(Path dir) {
        if (dir == null) {
            throw new IllegalArgumentException("dir must not be null");
        }
        try {
            return Files.createDirectories(dir);
        } catch (IOException ex) {
            throw new IllegalStateException("create directory failed: " + dir, ex);
        }
    }

    /**
     * 确保目录存在。
     *
     * @param dir 目录路径
     * @return 目录 File
     */
    public static File ensureDir(String dir) {
        if (StringUtils.isBlank(dir)) {
            throw new IllegalArgumentException("dir must not be blank");
        }
        return ensureDir(Path.of(dir)).toFile();
    }

    /**
     * 判断路径是否为已存在的普通文件。
     *
     * @param path 路径
     * @return 是普通文件则为 {@code true}
     */
    public static boolean isFile(Path path) {
        return path != null && Files.isRegularFile(path);
    }

    /**
     * 判断路径是否为已存在的目录。
     *
     * @param path 路径
     * @return 是目录则为 {@code true}
     */
    public static boolean isDirectory(Path path) {
        return path != null && Files.isDirectory(path);
    }

    /**
     * 以 UTF-8 读取文本文件。
     *
     * @param path 文件路径
     * @return 文件内容
     */
    public static String readUtf8(Path path) {
        return readString(path, StandardCharsets.UTF_8);
    }

    /**
     * 按指定字符集读取文本文件。
     *
     * @param path    文件路径
     * @param charset 字符集
     * @return 文件内容
     */
    public static String readString(Path path, Charset charset) {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null");
        }
        try {
            return Files.readString(path, charset == null ? StandardCharsets.UTF_8 : charset);
        } catch (IOException ex) {
            throw new IllegalStateException("read file failed: " + path, ex);
        }
    }

    /**
     * 以 UTF-8 写入文本（覆盖），自动创建父目录。
     *
     * @param path    文件路径
     * @param content 内容，null 按空串
     */
    public static void writeUtf8(Path path, String content) {
        writeString(path, content, StandardCharsets.UTF_8);
    }

    /**
     * 写入文本（覆盖），自动创建父目录。
     *
     * @param path    文件路径
     * @param content 内容，null 按空串
     * @param charset 字符集
     */
    public static void writeString(Path path, String content, Charset charset) {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null");
        }
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(path, content == null ? "" : content,
                    charset == null ? StandardCharsets.UTF_8 : charset);
        } catch (IOException ex) {
            throw new IllegalStateException("write file failed: " + path, ex);
        }
    }

    /**
     * 写入字节（覆盖），自动创建父目录。
     *
     * @param path 文件路径
     * @param data 字节，null 按空数组
     */
    public static void writeBytes(Path path, byte[] data) {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null");
        }
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(path, data == null ? new byte[0] : data);
        } catch (IOException ex) {
            throw new IllegalStateException("write bytes failed: " + path, ex);
        }
    }

    /**
     * 读取全部字节。
     *
     * @param path 文件路径
     * @return 文件字节
     */
    public static byte[] readBytes(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null");
        }
        try {
            return Files.readAllBytes(path);
        } catch (IOException ex) {
            throw new IllegalStateException("read bytes failed: " + path, ex);
        }
    }

    /**
     * 复制文件（覆盖目标），自动创建目标父目录。
     *
     * @param source 源文件
     * @param target 目标文件
     */
    public static void copy(Path source, Path target) {
        if (source == null || target == null) {
            throw new IllegalArgumentException("source/target must not be null");
        }
        try {
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("copy file failed: " + source + " -> " + target, ex);
        }
    }

    /**
     * 将输入流写入文件（覆盖），自动创建父目录；不关闭输入流。
     *
     * @param input 输入流
     * @param path  目标文件
     * @return 写入字节数
     */
    public static long copy(InputStream input, Path path) {
        if (input == null || path == null) {
            throw new IllegalArgumentException("input/path must not be null");
        }
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (OutputStream out = Files.newOutputStream(path)) {
                return input.transferTo(out);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("copy stream failed: " + path, ex);
        }
    }

    /**
     * 删除文件或空目录；不存在时静默成功。
     *
     * @param path 路径
     * @return 实际删除则为 {@code true}
     */
    public static boolean deleteQuietly(Path path) {
        if (path == null) {
            return false;
        }
        try {
            return Files.deleteIfExists(path);
        } catch (IOException ex) {
            return false;
        }
    }
}
