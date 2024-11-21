package me.clarius.sdk.cast.example

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import me.clarius.sdk.ImageFormat
import me.clarius.sdk.ProcessedImageInfo
import java.nio.ByteBuffer
import java.util.concurrent.Executor

/**
 * Convert images in a separate thread to avoid blocking the producer (the SDK)
 */
class ImageConverter internal constructor(
    private val executor: Executor,
    private val callback: Callback
) {
    fun convertImage(buffer: ByteBuffer, info: ProcessedImageInfo) {
        executor.execute {
            try {
                val bitmap = convert(buffer, info)
                callback.onResult(bitmap, info.tm)
            } catch (e: Exception) {
                callback.onError(e)
            }
        }
    }

    private fun convert(buffer: ByteBuffer, info: ProcessedImageInfo): Bitmap {
        val isCompressed = info.format != ImageFormat.Uncompressed
        val bitmap: Bitmap
        if (isCompressed) {
            if (buffer.hasArray()) {
                val bytes = buffer.array()
                val offset = buffer.arrayOffset()
                val length = info.imageSize
                assert(offset + length < bytes.size)
                bitmap = BitmapFactory.decodeByteArray(bytes, offset, length)
            } else {
                val bytes = ByteArray(buffer.capacity())
                buffer[bytes]
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
        } else {
            bitmap = Bitmap.createBitmap(info.width, info.height, Bitmap.Config.ARGB_8888)
            bitmap.copyPixelsFromBuffer(buffer)
        }
        if (bitmap == null) throw AssertionError("bad image data")
        return bitmap
    }

    internal interface Callback {
        fun onResult(bitmap: Bitmap, timestamp: Long)

        fun onError(e: Exception)
    }
}
