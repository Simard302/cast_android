package me.clarius.sdk.cast.example.overlay

import android.content.ComponentName
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import me.clarius.sdk.Cast
import me.clarius.sdk.ProbeInfo
import me.clarius.sdk.cast.example.CastService
import me.clarius.sdk.cast.example.CastService.CastBinder
import me.clarius.sdk.cast.example.FirstFragment
import me.clarius.sdk.cast.example.FirstFragment.Companion
import me.clarius.sdk.cast.example.R
import me.clarius.sdk.cast.example.SlidesFragmentArgs
import me.clarius.sdk.cast.example.databinding.FragmentOverlayBinding
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.StandardCharsets
import java.util.Optional

class OverlayFragment : Fragment() {
    private var castService: CastService? = null
    private var castBinder: CastBinder? = null
    private var binding: FragmentOverlayBinding? = null
    var model: TfModel? = null
    private var timestamp: Long? = 0L


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOverlayBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    private val castConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to our service, cast the IBinder now
            castBinder = service as CastBinder

            /** Nerve Model  */
            // Initialize tf model on service connected
            model = TfModel()

            val procedure = SlidesFragmentArgs.fromBundle(arguments!!).procedure

            model!!.setNewModel(procedure)


            // Observe the processed image LiveData
            castBinder!!.getProcessedImage().observe(
                viewLifecycleOwner
            ) { processedImage: Bitmap ->
                // Apply your image manipulation logic here
                val manipulatedImage = model!!.process(processedImage)
                binding!!.imageView.setImageBitmap(manipulatedImage)
            }

            Log.d(TAG, "model created")

            castBinder!!.timestamp.observe(
                viewLifecycleOwner
            ) { timestamp: Long? -> this@OverlayFragment.setTimestamp(timestamp) }
            castBinder!!.getError().observe(
                viewLifecycleOwner
            ) { text: String? -> this@OverlayFragment.showError(text) }
            castBinder!!.getRawDataProgress().observe(
                viewLifecycleOwner
            ) { progress: Int? ->
                binding!!.rawDataDownloadProgressBar.progress =
                    progress!!
            }
        }

        override fun onServiceDisconnected(component: ComponentName) {
            Log.d(TAG, "service disconnected")
            castBinder = null
        }
    }

    inner class TfModel {
        private var tflite: Interpreter? = null
        private var currentModelTag = "TAP"

        init {
            try {
                tflite = Interpreter(loadModelFile("TAP.tflite"))
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        private fun getModelFile(modelTag: String): String {
            return when (modelTag) {
                "Transabdominal Plane Block" -> "TAP.tflite"
                "Brachial Plexus Block" -> "BP.tflite"
                "Femoral Nerve Block" -> "FN_All_Aug_exp1.tflite"
                else -> "none"
            }
        }

        fun setNewModel(modelTag: String) {
            Log.d(TAG, "model tag: $modelTag")
            if (tflite != null) {
                tflite!!.close()
                Log.d(TAG, "Closed Current model resources")
            }
            val modelFile = getModelFile(modelTag)
            try {
                tflite = Interpreter((loadModelFile(modelFile)))
                currentModelTag = modelTag
                Log.d(TAG, "New model set to: $modelFile")
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
            val inputShape = tflite!!.getInputTensor(0).shape()
            println("Model input shape: " + inputShape.contentToString())
        }

        @Throws(IOException::class)
        fun loadModelFile(modelFile: String): MappedByteBuffer {
            // Load the model file from your assets folder
            val fileDescriptor = activity!!.assets.openFd(modelFile)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        }

        fun process(inputImage: Bitmap): Bitmap {
            // Resize the input image to match the expected input size of the model
            val resizedBitmap = Bitmap.createScaledBitmap(inputImage, 128, 128, false)

            // Convert the Bitmap to a TensorImage
            val inputTensor = TensorImage(DataType.FLOAT32)
            inputTensor.load(resizedBitmap)

            // Define the output shape and type based on the model's output
            val outputBuffer =
                TensorBuffer.createFixedSize(intArrayOf(1, 128, 128, 1), DataType.FLOAT32)

            // Run the inference
            tflite!!.run(inputTensor.buffer, outputBuffer.buffer)

            // Post-process the output
            return postprocessOutput(outputBuffer, inputImage)
        }

        private fun postprocessOutput(outputBuffer: TensorBuffer, originalImage: Bitmap): Bitmap {
            val maskWidth = 128
            val maskHeight = 128
            val originalWidth = originalImage.width
            val originalHeight = originalImage.height

            // Create a bitmap for the mask
            val maskBitmap = Bitmap.createBitmap(maskWidth, maskHeight, Bitmap.Config.ARGB_8888)

            // Define a semi-transparent yellow color for the mask
            val semiTransparentYellow = Color.argb(128, 255, 255, 0)

            // Create the mask bitmap based on the outputBuffer
            for (x in 0 until maskWidth) {
                for (y in 0 until maskHeight) {
                    val value = outputBuffer.getFloatValue(y * maskWidth + x)
                    if (value > 0.5) {
                        maskBitmap.setPixel(x, y, semiTransparentYellow)
                    } else {
                        // Use a transparent color for areas outside the mask
                        maskBitmap.setPixel(x, y, Color.TRANSPARENT)
                    }
                }
            }

            // Scale the mask bitmap to match the original image size
            val scaledMask =
                Bitmap.createScaledBitmap(maskBitmap, originalWidth, originalHeight, false)

            // Create a new bitmap to combine original image and the scaled mask
            val combinedBitmap =
                Bitmap.createBitmap(originalWidth, originalHeight, Bitmap.Config.ARGB_8888)

            // Draw the original image and mask overlay onto the combined bitmap
            val canvas = Canvas(combinedBitmap)
            canvas.drawBitmap(originalImage, 0f, 0f, null)
            canvas.drawBitmap(scaledMask, 0f, 0f, null)

            return combinedBitmap
        }
    }

    private fun setTimestamp(timestamp: Long?) {
        this.timestamp = timestamp
    }

    private fun showError(text: CharSequence?) {
        Log.e(OverlayFragment.TAG, "Error: $text")
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post { Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show() }
    }

    companion object {
        private const val TAG = "Overlay"

        private fun fromByteArray(from: ByteArray): String {
            return String(from, StandardCharsets.UTF_8)
        }

        private fun askProbeInfo(cast: Cast) {
            cast.getProbeInfo { info: Optional<ProbeInfo> ->
                Log.d(
                    TAG,
                    "Probe info: " + info.map { info: ProbeInfo ->
                        probeInfoToString(info)
                    }.orElse("<none>")
                )
            }
        }

        private fun probeInfoToString(info: ProbeInfo): String {
            val b = StringBuilder()
            b.append("v").append(info.version).append(" elements: ").append(info.elements)
                .append(" pitch: ").append(info.pitch).append(" radius: ").append(info.radius)
            return b.toString()
        }
    }
}