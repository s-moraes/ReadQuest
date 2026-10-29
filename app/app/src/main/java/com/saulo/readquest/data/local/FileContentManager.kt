package com.saulo.readquest.data.local

import com.saulo.readquest.data.model.CatalogDto
import com.saulo.readquest.data.model.SubjectManifestDto
import kotlinx.serialization.json.Json
import java.io.File

class FileContentManager(
    val contentDir: File
) {
    val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    fun readCatalog(): CatalogDto? {
        val file = File(contentDir, "catalog.json")
        if (!file.exists()) return null
        return try {
            json.decodeFromString<CatalogDto>(file.readText())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun readSubjectManifest(subjectId: String): SubjectManifestDto? {
        val subjectDir = getSubjectDir(subjectId)
        val file = File(subjectDir, "subject.json").let {
            if (it.exists()) it else File(subjectDir, "course.json")
        }
        if (!file.exists()) return null
        return try {
            json.decodeFromString<SubjectManifestDto>(file.readText())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun readSubjectFile(subjectId: String, relativePath: String): String? {
        val subjectDir = getSubjectDir(subjectId)
        val file = File(subjectDir, relativePath)
        if (!file.exists()) return null
        return try {
            file.readText()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getSubjectDir(subjectId: String): File {
        return File(contentDir, subjectId)
    }
}
