package me.clarius.sdk.cast.example

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.MutableLiveData
import me.clarius.sdk.Button
import me.clarius.sdk.Cast
import me.clarius.sdk.Platform
import me.clarius.sdk.PosInfo
import me.clarius.sdk.ProcessedImageInfo
import me.clarius.sdk.RawImageInfo
import me.clarius.sdk.SpectralImageInfo
import me.clarius.sdk.TgcInfo
import java.nio.ByteBuffer
import java.util.Optional
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CastService : Service() {
    private val processedImage = MutableLiveData<Bitmap>()
    private val imageTime = MutableLiveData<Long?>()
    private val error = MutableLiveData<String?>()
    private val rawDataProgress = MutableLiveData<Int?>()
    private val binder: IBinder = CastBinder()
    private val executorService: ExecutorService = Executors.newFixedThreadPool(1)
    private val converter = ImageConverter(executorService, object : ImageConverter.Callback {
        override fun onResult(bitmap: Bitmap, timestamp: Long) {
            processedImage.postValue(bitmap)
            imageTime.postValue(timestamp)
        }

        override fun onError(e: Exception) {
            error.postValue(e.toString())
        }
    })
    private val listener: Cast.Listener = object : Cast.Listener {
        override fun error(e: String) {
            error.postValue(e)
        }

        override fun freeze(frozen: Boolean) {
            Log.d(TAG, "Freeze: $frozen")
        }

        override fun newProcessedImage(
            data: ByteBuffer,
            info: ProcessedImageInfo,
            pos: Array<PosInfo>
        ) {
            converter.convertImage(data, info)
        }

        override fun newRawImageFn(data: ByteBuffer, info: RawImageInfo, pos: Array<PosInfo>) {
        }

        override fun newSpectralImageFn(data: ByteBuffer, info: SpectralImageInfo) {
        }

        override fun newImuDataFn(pos: Array<PosInfo>) {
        }

        override fun progress(i: Int) {
            rawDataProgress.postValue(i)
        }

        override fun buttonPressed(button: Button, count: Int) {
            Log.d(TAG, "Button '$button pressed $count time(s)")
        }
    }
    private var cast: Cast? = null

    override fun onCreate() {
        super.onCreate()
        if (cast == null) {
            Log.d(TAG, "Creating the Cast service")
            cast = Cast(applicationContext.applicationInfo.nativeLibraryDir, listener)
            cast!!.initialize(
                getCertDir(this)
            ) { result ->
                Log.d(TAG, "Initialization result: $result")
                if (result) {
                    cast!!.getFirmwareVersion(
                        Platform.V1
                    ) { optional: Optional<String> ->
                        Log.i(
                            TAG,
                            "Firmware " + Platform.V1 + ": " + optional.orElse(
                                NONE
                            )
                        )
                    }
                    cast!!.getFirmwareVersion(
                        Platform.HD
                    ) { optional: Optional<String> ->
                        Log.i(
                            TAG,
                            "Firmware " + Platform.HD + ": " + optional.orElse(
                                NONE
                            )
                        )
                    }
                    cast!!.getFirmwareVersion(
                        Platform.HD3
                    ) { optional: Optional<String> ->
                        Log.i(
                            TAG,
                            "Firmware " + Platform.HD3 + ": " + optional.orElse(
                                NONE
                            )
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (cast != null) {
            Log.d(TAG, "Destroying the Cast service")
            cast!!.disconnect(null)
            cast!!.release()
            cast = null
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG, "Binding to the Cast service")
        return binder
    }

    /**
     * The Binder is the interface to our service.
     */
    inner class CastBinder : Binder() {
        fun getCast(): Cast? {
            return cast
        }

        fun getProcessedImage(): MutableLiveData<Bitmap> {
            return processedImage
        }

        val timestamp: MutableLiveData<Long?>
            get() = imageTime

        fun getError(): MutableLiveData<String?> {
            return error
        }

        fun getRawDataProgress(): MutableLiveData<Int?> {
            return rawDataProgress
        }
    }

    companion object {
        private const val TAG = "Cast"
        private const val NONE = "<none>"
        private fun getCertDir(context: Context): String {
            return context.getDir("cert", MODE_PRIVATE).toString()
        }
    }
}
