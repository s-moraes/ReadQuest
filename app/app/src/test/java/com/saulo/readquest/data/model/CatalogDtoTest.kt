package com.saulo.readquest.data.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CatalogDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun catalogJson_deserializesCorrectly() {
        val catalogJsonFile = File("../../content/catalog.json").let {
            if (it.exists()) it else File("src/test/resources/content/catalog.json")
        }
        val rawJson = catalogJsonFile.readText()

        val catalog = json.decodeFromString<CatalogDto>(rawJson)
        assertNotNull(catalog)
        assertTrue(catalog.subjects.isNotEmpty())

        val guide = catalog.subjects.firstOrNull { it.id == "readquest-guide" }
        assertNotNull(guide)
        assertEquals("Mastering ReadQuest: The Complete App Guide", guide?.title)
        assertEquals("content/readquest-guide/subject.json", guide?.path)
    }
}
