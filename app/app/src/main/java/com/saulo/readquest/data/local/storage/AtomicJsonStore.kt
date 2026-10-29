package com.saulo.readquest.data.local.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream

/**
 * General-purpose, thread-safe, coroutine-mutex backed JSON file store.
 * Uses atomic file operations to prevent file corruption.
 */
class AtomicJsonStore<T>(
    val file: File,
    private val serializer: KSerializer<T>,
    private val defaultFactory: () -> T,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }
) {
    private val mutex = Mutex()
    private val _stateFlow = MutableStateFlow(readInitial())
    val stateFlow: StateFlow<T> = _stateFlow.asStateFlow()

    private fun readInitial(): T {
        return try {
            if (file.exists() && file.length() > 0) {
                json.decodeFromString(serializer, file.readText())
            } else {
                defaultFactory()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            defaultFactory()
        }
    }

    suspend fun get(): T = stateFlow.value

    suspend fun update(transform: (T) -> T): T = withContext(Dispatchers.IO) {
        mutex.withLock {
            val current = _stateFlow.value
            val next = transform(current)
            writeToDisk(next)
            _stateFlow.value = next
            next
        }
    }

    suspend fun reset(): T = withContext(Dispatchers.IO) {
        mutex.withLock {
            val defaultVal = defaultFactory()
            if (file.exists()) {
                file.delete()
            }
            _stateFlow.value = defaultVal
            defaultVal
        }
    }

    private fun writeToDisk(value: T) {
        val parent = file.parentFile
        if (parent != null && !parent.exists()) {
            parent.mkdirs()
        }

        val jsonString = json.encodeToString(serializer, value)
        val bytes = jsonString.toByteArray(Charsets.UTF_8)

        // Try Android AtomicFile if available at runtime
        var writtenViaAtomicFile = false
        try {
            val atomicFileClass = Class.forName("android.util.AtomicFile")
            val constructor = atomicFileClass.getConstructor(File::class.java)
            val atomicFileInstance = constructor.newInstance(file)
            val startWriteMethod = atomicFileClass.getMethod("startWrite")
            val finishWriteMethod = atomicFileClass.getMethod("finishWrite", FileOutputStream::class.java)

            val outStream = startWriteMethod.invoke(atomicFileInstance) as FileOutputStream
            outStream.write(bytes)
            finishWriteMethod.invoke(atomicFileInstance, outStream)
            writtenViaAtomicFile = true
        } catch (_: Throwable) {
            writtenViaAtomicFile = false
        }

        if (!writtenViaAtomicFile) {
            // Standard atomic file rename fallback for JVM test environments
            val tempFile = File(parent ?: File("."), "${file.name}.tmp")
            FileOutputStream(tempFile).use { it.write(bytes) }
            if (!tempFile.renameTo(file)) {
                file.delete()
                if (!tempFile.renameTo(file)) {
                    tempFile.copyTo(file, overwrite = true)
                    tempFile.delete()
                }
            }
        }
    }
}
