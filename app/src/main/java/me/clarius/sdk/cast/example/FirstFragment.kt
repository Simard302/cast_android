package me.clarius.sdk.cast.example

import android.content.ComponentName
import android.content.Context
import android.content.Intent
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
import me.clarius.sdk.TgcInfo
import me.clarius.sdk.BuildConfig
import me.clarius.sdk.LineF
import me.clarius.sdk.Platform
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import me.clarius.sdk.Cast
import me.clarius.sdk.ProbeInfo
import me.clarius.sdk.UserFunction
import me.clarius.sdk.cast.example.CastService.CastBinder
import me.clarius.sdk.cast.example.databinding.FragmentFirstBinding
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.StandardCharsets
import java.util.Optional

class FirstFragment : Fragment() {
    private var binding: FragmentFirstBinding? = null
    private var castBinder: CastBinder? = null
    private var timestamp: Long? = 0L

    /** Nerve Model  */ // create model
    var model: TfModel? = null

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
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
            ) { timestamp: Long? -> this@FirstFragment.setTimestamp(timestamp) }
            castBinder!!.getError().observe(
                viewLifecycleOwner
            ) { text: String? -> this@FirstFragment.showError(text) }
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding!!.buttonConnect.setOnClickListener { _: View? -> doConnect() }
        binding!!.buttonRun.setOnClickListener { _: View? -> toggleRun() }
        binding!!.buttonDisconnect.setOnClickListener { _: View? -> doDisconnect() }
    }

    override fun onResume() {
        super.onResume()
        val intent = requireActivity().intent
        if (intent != null) {
            val extras = intent.extras
            if (extras != null) {
                val probeSerial = Optional.ofNullable(extras.getByteArray("cus_probe_serial"))
                    .map { from: ByteArray ->
                        fromByteArray(from)
                    }
                val ipAddress = Optional.ofNullable(extras.getByteArray("cus_ip_address"))
                    .map { from: ByteArray ->
                        fromByteArray(from)
                    }
                val castPort = Optional.ofNullable(extras.getByteArray("cus_cast_port"))
                    .map { from: ByteArray ->
                        fromByteArray(from)
                    }
                val networkId = Optional.ofNullable(extras.getByteArray("cus_network_id"))
                    .map { from: ByteArray ->
                        fromByteArray(from)
                    }
                Log.d(TAG, "Received probe serial: " + probeSerial.orElse("<none>"))
                Log.d(TAG, "Received IP address: " + ipAddress.orElse("<none>"))
                Log.d(TAG, "Received cast port: " + castPort.orElse("<none>"))
                Log.d(TAG, "Received network ID: " + networkId.orElse("<none>"))
                ipAddress.ifPresent { s: String? -> binding!!.ipAddress.setText(s) }
                castPort.ifPresent { s: String? -> binding!!.tcpPort.setText(s) }
                networkId.ifPresent { s: String? -> binding!!.networkId.setText(s) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = requireContext()
        val intent = Intent(context, CastService::class.java)
        context.bindService(intent, castConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        val context = requireContext()
        context.unbindService(castConnection)
        castBinder = null
    }

    private fun toggleRun() {
        if (castBinder == null) {
            showError("Clarius Cast not initialized")
            return
        }
        showMessage("Toggle run")
        castBinder!!.getCast()!!.userFunction(
            UserFunction.Freeze, 0.0
        ) { result: Boolean ->
            Log.d(
                TAG,
                "Freeze function result: $result"
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun doConnect() {
        if (castBinder == null) {
            showError("Clarius Cast not initialized")
            return
        }
        binding!!.ipAddressLayout.error = null
        binding!!.tcpPortLayout.error = null
        val ipAddress = binding!!.ipAddress.text.toString()
        if (ipAddress.isEmpty()) {
            binding!!.ipAddressLayout.error = "Cannot be empty"
            return
        }
        val tcpPort = Utils.maybeInt(binding!!.tcpPort.text)
        if (!tcpPort.isPresent) {
            binding!!.tcpPortLayout.error = "Invalid number"
            return
        }
        val networkId = Utils.maybeLong(
            binding!!.networkId.text
        )
        showMessage("Connecting to " + ipAddress + ":" + tcpPort.get())
        castBinder!!.interfaceDescriptor
        castBinder!!.getCast()!!.connect(
            ipAddress, tcpPort.get(), networkId, certificate
        ) { result: Boolean, port: Int, swRevMatch: Boolean ->
            Log.d(
                TAG,
                "Connection result: $result"
            )
            if (result) {
                Log.d(TAG, "UDP stream will be on port $port")
                Log.d(
                    TAG,
                    "App software " + (if (swRevMatch) "matches" else "does not match")
                )
                askProbeInfo(castBinder!!.getCast()!!)
            }
        }
    }

    private fun doDisconnect() {
        if (castBinder == null) {
            return
        }
        castBinder!!.getCast()!!.disconnect { result: Boolean ->
            Log.d(
                TAG,
                "Disconnection result: $result"
            )
        }
    }

    private val rawData: Unit
        get() {
            if (castBinder == null) {
                return
            }
            showMessage("Requesting raw data")
            binding!!.rawDataDownloadProgressBar.progress = 0
            val cast = castBinder!!.getCast()
            val handle = RawDataFile(cast)
            val start = 0
            val end = 0
            val lzo = true
            cast!!.requestRawData(
                start.toLong(), end.toLong(), lzo
            ) { result: Int -> handle.requestResultRetrieved(result) }
        }

    private fun doCapture() {
        if (castBinder == null || timestamp == 0L) {
            return
        }
        showMessage("Starting image capture")
        castBinder!!.getCast()!!.startCapture(timestamp!!) { captureID: Int ->
            Log.d(
                TAG,
                "Start capture got ID: $captureID"
            )
            if (captureID < 0) {
                return@startCapture
            }
            castBinder!!.getCast()!!.finishCapture(
                captureID
            ) { result: Boolean ->
                Log.d(
                    TAG,
                    "Finish capture got result: $result"
                )
            }
        }
    }

    private fun setTimestamp(timestamp: Long?) {
        this.timestamp = timestamp
    }

    private fun showError(text: CharSequence?) {
        Log.e(TAG, "Error: $text")
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post { Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show() }
    }

    private fun showMessage(text: CharSequence) {
        Log.d(TAG, (text as String))
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post { Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show() }
    }

    private val certificate: String
        get() = "research"

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

    private inner class RawDataFile(val cast: Cast?) {
        fun requestResultRetrieved(result: Int) {
            if (result > 0) {
                showMessage("Found raw data")
                cast!!.readRawData { result: Int, data: ByteBuffer ->
                    this.rawDataRetrieved(
                        result,
                        data
                    )
                }
            } else {
                showMessage("Failed to request raw data, ensure raw data buffering is enabled and image is frozen")
            }
        }

        fun rawDataRetrieved(result: Int, data: ByteBuffer) {
            if (result > 0) {
                try {
                    showMessage("Saving raw data")
                    val uri = IOUtils.saveInDocuments(data, "cast_raw_data", requireContext())
                    showMessage("Saved raw data in file $uri")
                } catch (e: IOException) {
                    showError(e.toString())
                }
            } else {
                showError("Could not retrieve raw data")
            }
        }
    }

    companion object {
        private const val TAG = "Cast"

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
