package de.unixkiwi.betterschool.core.local

import android.content.Context
import android.util.Base64
import java.io.File

class ApiCacheFileManager(
    private val context: Context
) {
    private val cacheDir = File(context.filesDir, "api_cache").also { it.mkdirs() }

    private fun String.toSafeFileName() = Base64.encodeToString(
        this.toByteArray(),
        Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
    )

    /*private fun String.fromSafeFileName() =
        String(
            Base64.decode(this, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING),
            StandardCharsets.UTF_8
        )*/


    fun store(key: String, content: String) {
        File(cacheDir, "${key.toSafeFileName()}.json").writeText(content)
    }

    fun get(key: String): String? {
        val file = File(cacheDir, "${key.toSafeFileName()}.json")
        return if (file.exists()) file.readText(Charsets.UTF_8) else null
    }

    fun delete(key: String) {
        File(cacheDir, "${key.toSafeFileName()}.json").delete()
    }
}