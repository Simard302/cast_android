package me.clarius.sdk.cast.example.overlay

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.ColorStateList
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
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import me.clarius.sdk.Cast
import me.clarius.sdk.ProbeInfo
import me.clarius.sdk.UserFunction
import me.clarius.sdk.cast.example.R
import me.clarius.sdk.cast.example.clarius.CastService
import me.clarius.sdk.cast.example.clarius.CastService.CastBinder
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
    private var castBinder: CastBinder? = null
    private var binding: FragmentOverlayBinding? = null
    var model: TfModel? = null
    private var timestamp: Long? = 0L

    // Toggle buttons and slider values
    private var started: Boolean = false
    private var showNeedleOverlay: Boolean = false
    private var showNerveOverlay: Boolean = false
    private var insertionSideLeft: Boolean = true
    // TODO add gain, visibility, etc
    // TODO add stuff for (if frozen, and add that logic to the toggleRun)


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOverlayBinding.inflate(inflater, container, false)

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val startButton: Button = view.findViewById(R.id.btnStart)
        startButton.setOnClickListener {
            val res = this.toggleRun()
            if (res) {
                if (this.started) {
                    startButton.text = "START"
                    startButton.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.green
                        )
                    )
                } else {
                    startButton.text = "STOP"
                    startButton.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.red
                        )
                    )
                }
                this.started = !this.started
            }
        }

        val settingsButton: ImageButton = view.findViewById(R.id.btnSettings)
        settingsButton.setOnClickListener {
            findNavController().navigate(R.id.pair_device)
        }

        val directionButton: Button = view.findViewById(R.id.desiredInsertDirection)

        directionButton.setOnClickListener {
            if (this.insertionSideLeft) {
                directionButton.text = "Insertion Side:\nRight"
            } else {
                directionButton.text = "Insertion Side:\nLeft"
            }
            this.insertionSideLeft = !this.insertionSideLeft
        }

        val needleButton: LinearLayout = view.findViewById(R.id.btnNeedle)
        val needleVisibility: ImageView = view.findViewById(R.id.needleVisibility)

        needleButton.setOnClickListener {
            if (this.showNeedleOverlay) {
                needleVisibility.setImageResource(R.drawable.hide)
                needleButton.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
            } else {
                needleVisibility.setImageResource(R.drawable.show)
                needleButton.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.purple
                    )
                )
            }
            this.showNeedleOverlay = !this.showNeedleOverlay
        }

        val nerveButton: LinearLayout = view.findViewById(R.id.btnNerve)
        val nerveVisibility: ImageView = view.findViewById(R.id.nerveVisibility)

        nerveButton.setOnClickListener {
            if (this.showNerveOverlay) {
                nerveVisibility.setImageResource(R.drawable.hide)
                nerveButton.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
            } else {
                nerveVisibility.setImageResource(R.drawable.show)
                nerveButton.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.purple
                    )
                )
            }
            this.showNerveOverlay = !this.showNerveOverlay
        }

        val safeZoneSlider: SeekBar = view.findViewById(R.id.seekBarSafeZone)
        val safeZoneValueText: TextView = view.findViewById(R.id.tvSafeZoneValue)

        safeZoneSlider.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                safeZoneValueText.text = "$progress x"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?){
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        val gainSlider: SeekBar = view.findViewById(R.id.seekBarGain)
        val gainValueText: TextView = view.findViewById(R.id.tvGainValue)

        gainSlider.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val gain = progress.toDouble() / 2
                gainValueText.text = "$gain x"
                castBinder!!.getCast()!!.userFunction(
                    UserFunction.SetGain, gain*100
                ) { result: Boolean ->
                    Log.d(
                        TAG,
                        "Gain function result: $result"
                    )
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?){
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        val freqSlider: SeekBar = view.findViewById(R.id.seekBarFrequency)
        val freqTextValue: TextView = view.findViewById(R.id.tvFrequencyValue)

        freqSlider.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val freq = progress.toDouble() / 2
                freqTextValue.text = "$freq MHz"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?){
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
    }

    override fun onResume() {
        super.onResume()
        val intent = requireActivity().intent
        if (intent != null) {
            val extras = intent.extras
            if (extras != null) {
                val probeSerial =
                    Optional.ofNullable<ByteArray>(extras.getByteArray("cus_probe_serial"))
                        .map<String>(OverlayFragment::fromByteArray)
                val ipAddress =
                    Optional.ofNullable<ByteArray>(extras.getByteArray("cus_ip_address"))
                        .map<String>(OverlayFragment::fromByteArray)
                val castPort = Optional.ofNullable<ByteArray>(extras.getByteArray("cus_cast_port"))
                    .map<String>(OverlayFragment::fromByteArray)
                val networkId =
                    Optional.ofNullable<ByteArray>(extras.getByteArray("cus_network_id"))
                        .map<String>(OverlayFragment::fromByteArray)
                Log.d(TAG, "Received probe serial: " + probeSerial.orElse("<none>"))
                Log.d(TAG, "Received IP address: " + ipAddress.orElse("<none>"))
                Log.d(TAG, "Received cast port: " + castPort.orElse("<none>"))
                Log.d(TAG, "Received network ID: " + networkId.orElse("<none>"))
//                ipAddress.ifPresent { s: String? ->
//                    binding.ipAddress.setText(
//                        s
//                    )
//                }
//                castPort.ifPresent { s: String? ->
//                    binding.tcpPort.setText(
//                        s
//                    )
//                }
//                networkId.ifPresent { s: String? ->
//                    binding.networkId.setText(
//                        s
//                    )
//                }
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

    private fun toggleRun(): Boolean {
        if (castBinder == null) {
            showError("Clarius Cast not initialized")
            return false
        }
        val cast = castBinder!!.getCast()
//        if (!cast!!.isConnected) {
//            showError("Clarius device is not yet connected")
//            return false
//        }
        castBinder!!.getCast()!!.userFunction(
            UserFunction.Freeze, 0.0
        ) { result: Boolean ->
            Log.d(
                TAG,
                "Freeze function result: $result"
            )
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private val castConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to our service, cast the IBinder now
            castBinder = service as CastBinder


            /** Nerve Model  */
            // Initialize tf model on service connected
            model = TfModel()

            var processingImage = false
            // Observe the processed image LiveData
            castBinder!!.getProcessedImage().observe(
                requireActivity()
            ) { processedImage: Bitmap ->
                if (!processingImage) {
                    Log.d(TAG, "processed image blabla")
                    if (binding != null) {
                        processingImage = true
                        Log.d(TAG, "binding is here")   // TODO for some reason more than 1 can pass here
                        val modifiedImage = model!!.process(processedImage)
                        binding!!.ultrasoundImage.setImageBitmap(modifiedImage)
                        processingImage = false
                    }
                }
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
                Log.d(TAG, "Raw data progress: $progress")
            }
        }

        override fun onServiceDisconnected(component: ComponentName) {
            Log.d(TAG, "service disconnected")
            castBinder = null
        }
    }

    inner class TfModel {
        private var tfliteNerve: Interpreter? = null
        private var tfliteNeedle: Interpreter? = null
        private val modelDims: Pair<Int, Int> = Pair(128, 128)
        // Bitmaps
        val nerveBitmap = Bitmap.createBitmap(modelDims.first, modelDims.second, Bitmap.Config.ARGB_8888)
        val needleBitmap = Bitmap.createBitmap(modelDims.first, modelDims.second, Bitmap.Config.ARGB_8888)

        init {
            try {
                tfliteNerve = Interpreter(loadModelFile("TAP-old.tflite"))
                tfliteNeedle = Interpreter(loadModelFile("needle.tflite"))
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
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
            val resizedBitmap = Bitmap.createScaledBitmap(inputImage, modelDims.first, modelDims.second, false)

            // Convert the Bitmap to a TensorImage
            val inputTensor = TensorImage(DataType.FLOAT32)
            inputTensor.load(resizedBitmap)

            val outputBufferNerve = TensorBuffer.createFixedSize(intArrayOf(1, modelDims.first, modelDims.second, 1), DataType.FLOAT32)
            val outputBufferNeedle = TensorBuffer.createFixedSize(intArrayOf(1, modelDims.first, modelDims.second, 1), DataType.FLOAT32)
    
            // Run both models
            if (showNerveOverlay){
                tfliteNerve?.run(inputTensor.buffer, outputBufferNerve.buffer)
            }
            if (showNeedleOverlay) {
                tfliteNeedle?.run(inputTensor.buffer, outputBufferNeedle.buffer)
            }
    
            // Process and overlay both outputs
            return postprocessOutput(outputBufferNerve, outputBufferNeedle, inputImage)
        }
    
        private fun postprocessOutput(outputNerveBuffer: TensorBuffer, outputNeedleBuffer: TensorBuffer, originalImage: Bitmap): Bitmap {
            val originalWidth = originalImage.width
            val originalHeight = originalImage.height
    
            // Define mask colors
            val semiTransparentYellow = Color.argb(128, 255, 255, 0) // Nerve
            val semiTransparentRed = Color.argb(128, 255, 0, 0) // Needle

            // TODO potential optimization is use single bitmap and either set the bits to yellow, red or merge if both
            for (x in 0 until this.modelDims.first) {
                for (y in 0 until this.modelDims.second) {
                    val needleValue = outputNeedleBuffer.getFloatValue(y * modelDims.first + x)
                    val nerveValue = outputNerveBuffer.getFloatValue(y * modelDims.first + x)
                    needleBitmap.setPixel(x, y, if (needleValue > 0.5 && showNeedleOverlay) semiTransparentRed else Color.TRANSPARENT)
                    nerveBitmap.setPixel(x, y, if (nerveValue > 0.5 && showNerveOverlay) semiTransparentYellow else Color.TRANSPARENT)
                }
            }
    
            // Scale the mask bitmaps to match the original image size
            val scaledNerveMask = Bitmap.createScaledBitmap(nerveBitmap, originalWidth, originalHeight, false)
            val scaledNeedleMask = Bitmap.createScaledBitmap(needleBitmap, originalWidth, originalHeight, false)
            
            // Create a new bitmap to combine the original image with the masks
            val combinedBitmap = Bitmap.createBitmap(originalWidth, originalHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(combinedBitmap)
            canvas.drawBitmap(originalImage, 0f, 0f, null)
            canvas.drawBitmap(scaledNerveMask, 0f, 0f, null)
            canvas.drawBitmap(scaledNeedleMask, 0f, 0f, null)
    
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

    private fun showMessage(text: CharSequence) {
        Log.d(TAG, (text as String))
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post {
            Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
        }
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