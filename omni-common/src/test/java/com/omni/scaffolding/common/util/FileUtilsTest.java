package com.omni.scaffolding.common.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void nameAndExtensionHelpers() {
        assertThat(FileUtils.getName("a/b/c.txt")).isEqualTo("c.txt");
        assertThat(FileUtils.getBaseName("a/b/c.txt")).isEqualTo("c");
        assertThat(FileUtils.getExtension("photo.JPEG")).isEqualTo("jpg");
        assertThat(FileUtils.getExtension("noext")).isEmpty();
        assertThat(FileUtils.sanitizeFileName("../etc/passwd")).isEqualTo("passwd");
        assertThat(FileUtils.sanitizeFileName("..")).isEqualTo("file");
        assertThat(FileUtils.isSafeRelativePath("biz/a.png")).isTrue();
        assertThat(FileUtils.isSafeRelativePath("../secret")).isFalse();
        assertThat(FileUtils.isSafeRelativePath("/abs")).isFalse();
    }

    @Test
    void formatSize() {
        assertThat(FileUtils.formatSize(500)).isEqualTo("500 B");
        assertThat(FileUtils.formatSize(1536)).isEqualTo("1.5 KB");
    }

    @Test
    void readWriteCopyDelete() {
        Path file = tempDir.resolve("demo/hello.txt");
        FileUtils.writeUtf8(file, "你好");
        assertThat(FileUtils.readUtf8(file)).isEqualTo("你好");
        assertThat(FileUtils.isFile(file)).isTrue();
        assertThat(FileUtils.isDirectory(tempDir.resolve("demo"))).isTrue();

        Path copy = tempDir.resolve("demo/copy.txt");
        FileUtils.copy(file, copy);
        assertThat(FileUtils.readBytes(copy)).isEqualTo("你好".getBytes(java.nio.charset.StandardCharsets.UTF_8));

        assertThat(FileUtils.deleteQuietly(copy)).isTrue();
        assertThat(Files.exists(copy)).isFalse();
    }

    @Test
    void ensureDirRejectsBlank() {
        assertThatThrownBy(() -> FileUtils.ensureDir("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
