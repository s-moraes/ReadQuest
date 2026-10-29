package com.saulo.readquest.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

open class HttpException(val statusCode: Int, message: String) : IOException(message)

interface RemoteContentFetcher {
    suspend fun fetchText(url: String): Result<String>
    suspend fun fetchBytes(url: String): Result<ByteArray>
}

class DefaultRemoteContentFetcher(
    private val connectTimeoutMs: Int = 15000,
    private val readTimeoutMs: Int = 30000
) : RemoteContentFetcher {

    override suspend fun fetchText(url: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = connectTimeoutMs
                readTimeout = readTimeoutMs
                requestMethod = "GET"
                setRequestProperty("User-Agent", "ReadQuest-Android")
            }

            try {
                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    throw HttpException(responseCode, "HTTP $responseCode while fetching $url")
                }
                connection.inputStream.bufferedReader().use { it.readText() }
            } finally {
                connection.disconnect()
            }
        }
    }

    override suspend fun fetchBytes(url: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        runCatching {
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = connectTimeoutMs
                readTimeout = readTimeoutMs
                requestMethod = "GET"
                setRequestProperty("User-Agent", "ReadQuest-Android")
            }

            try {
                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    throw HttpException(responseCode, "HTTP $responseCode while fetching bytes from $url")
                }
                val buffer = ByteArrayOutputStream()
                connection.inputStream.use { input ->
                    input.copyTo(buffer)
                }
                buffer.toByteArray()
            } finally {
                connection.disconnect()
            }
        }
    }
}
