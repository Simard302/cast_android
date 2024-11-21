package me.clarius.sdk.cast.example

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import java.io.IOException
import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object IOUtils {
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Throws(IOException::class)
    private fun save(buffer: ByteBuffer, prefix: String, context: Context): Uri {
        val fileName = String.format(
            "%s_%s.tar", prefix,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        )
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/x-tar")
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/Clarius")
        val contentResolver = context.contentResolver
        val uri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val itemUri = contentResolver.insert(uri, contentValues)
            ?: throw IOException("Failed to create the raw data file in the Documents folder")
        contentResolver.openOutputStream(itemUri).use { dest ->
            dest!!.write(buffer.array())
        }
        return itemUri
    }

    /**
     * Save the given byte buffer in the Documents folder.
     *
     *
     * NOTE: this method uses the MediaStore.Files.getContentUri() API which is only available on Android 10 and later.
     * Calling this method on older Android will raise an exception.
     *
     * @param buffer     the byte buffer to save.
     * @param context    the context to retrieve the Documents folder.
     * @return the saved file location.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun saveInDocuments(buffer: ByteBuffer, prefix: String, context: Context): Uri {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return save(buffer, prefix, context)
        } else {
            throw IOException("Saving only supported on Android 10 and later (API Q)")
        }
    }
}
