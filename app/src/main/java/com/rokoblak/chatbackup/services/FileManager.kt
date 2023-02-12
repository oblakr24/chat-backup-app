package com.rokoblak.chatbackup.services

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.rokoblak.chatbackup.R
import com.rokoblak.chatbackup.di.AppScope
import java.io.File
import java.time.Instant
import javax.inject.Inject

class FileManager @Inject constructor(private val scope: AppScope) {

    private val appContext = scope.appContext

    private fun createCacheFile(name: String): File {
        return File(appContext.cacheDir, name)
    }

    fun createNewJson(content: String): Uri {
        val timestamp = Instant.now().epochSecond
        val name = "export_$timestamp.json"
        val file = createCacheFile(name)
        file.writeText(content)

        return FileProvider.getUriForFile(
            appContext, appContext.getString(R.string.file_provider),
            file,
        )
    }

    fun share(context: Context, contentUri: Uri) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/json"

        intent.putExtra(Intent.EXTRA_STREAM, contentUri)

        context.startActivity(Intent.createChooser(intent, "Export .json file"))
        context.startActivity(intent)
    }
}