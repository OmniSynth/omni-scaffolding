package com.omni.scaffolding.infra.file;

import com.omni.scaffolding.common.file.StoreRequest;
import com.omni.scaffolding.common.file.StoredObject;
import com.omni.scaffolding.common.file.StoredObjectRef;
import com.omni.scaffolding.config.OmniFileProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalObjectStorageTest {

    @TempDir
    Path tempDir;

    private LocalObjectStorage storage;

    @BeforeEach
    void setUp() {
        OmniFileProperties props = new OmniFileProperties();
        props.getLocal().setBaseDir(tempDir.toString());
        storage = new LocalObjectStorage(props);
    }

    @Test
    void storeOpenAndDelete() throws Exception {
        byte[] payload = "hello-omni-file".getBytes(StandardCharsets.UTF_8);
        StoredObject stored = storage.store(new StoreRequest(
                "avatar/demo.txt",
                "text/plain",
                payload.length,
                new ByteArrayInputStream(payload),
                "demo.txt"));
        assertEquals("LOCAL", stored.storageType());
        assertTrue(tempDir.resolve("avatar/demo.txt").toFile().exists());

        byte[] read = storage.open(StoredObjectRef.from(stored)).readAllBytes();
        assertArrayEquals(payload, read);

        storage.delete(StoredObjectRef.from(stored));
        assertTrue(!tempDir.resolve("avatar/demo.txt").toFile().exists());
    }
}
