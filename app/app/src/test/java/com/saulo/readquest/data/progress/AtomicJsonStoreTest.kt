package com.saulo.readquest.data.progress

import com.saulo.readquest.data.model.LocalSettingsDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class AtomicJsonStoreTest {

    private lateinit var tempDir: File
    private lateinit var testFile: File

    @Before
    fun setUp() {
        tempDir = File(System.getProperty("java.io.tmpdir"), "readquest_atomic_test_${System.currentTimeMillis()}")
        tempDir.mkdirs()
        testFile = File(tempDir, "settings_test.json")
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun testDefaultStateWhenFileDoesNotExist() = runBlocking {
        val store = AtomicJsonStore(
            file = testFile,
            serializer = LocalSettingsDto.serializer(),
            defaultFactory = { LocalSettingsDto(themeMode = "SYSTEM", fontScale = 1.0f) }
        )

        val state = store.get()
        assertEquals("SYSTEM", state.themeMode)
        assertEquals(1.0f, state.fontScale, 0.001f)
        assertEquals("SYSTEM", store.stateFlow.value.themeMode)
        assertFalse(testFile.exists())
    }

    @Test
    fun testUpdatePersistsToDiskAndUpdatesStateFlow() = runBlocking {
        val store = AtomicJsonStore(
            file = testFile,
            serializer = LocalSettingsDto.serializer(),
            defaultFactory = { LocalSettingsDto() }
        )

        store.update { it.copy(themeMode = "DARK", fontScale = 1.15f) }

        assertTrue(testFile.exists())
        assertEquals("DARK", store.stateFlow.value.themeMode)
        assertEquals(1.15f, store.stateFlow.value.fontScale, 0.001f)

        // Read again with a new store pointing to the same file
        val newStore = AtomicJsonStore(
            file = testFile,
            serializer = LocalSettingsDto.serializer(),
            defaultFactory = { LocalSettingsDto() }
        )
        assertEquals("DARK", newStore.get().themeMode)
        assertEquals(1.15f, newStore.get().fontScale, 0.001f)
    }

    @Test
    fun testResetClearsFileAndRestoresDefaultState() = runBlocking {
        val store = AtomicJsonStore(
            file = testFile,
            serializer = LocalSettingsDto.serializer(),
            defaultFactory = { LocalSettingsDto(themeMode = "SYSTEM", fontScale = 1.0f) }
        )

        store.update { it.copy(themeMode = "LIGHT", fontScale = 1.3f) }
        assertTrue(testFile.exists())
        assertEquals("LIGHT", store.stateFlow.value.themeMode)

        store.reset()

        assertFalse(testFile.exists())
        assertEquals("SYSTEM", store.stateFlow.value.themeMode)
        assertEquals(1.0f, store.stateFlow.value.fontScale, 0.001f)
    }

    @Test
    fun testConcurrentUpdatesAreThreadSafe() = runBlocking(Dispatchers.IO) {
        val store = AtomicJsonStore(
            file = testFile,
            serializer = LocalSettingsDto.serializer(),
            defaultFactory = { LocalSettingsDto(fontScale = 0f) }
        )

        val iterations = 50
        coroutineScope {
            val jobs = (1..iterations).map {
                async {
                    store.update { current ->
                        current.copy(fontScale = current.fontScale + 1f)
                    }
                }
            }
            jobs.awaitAll()
        }

        assertEquals(iterations.toFloat(), store.get().fontScale, 0.001f)
    }
}
