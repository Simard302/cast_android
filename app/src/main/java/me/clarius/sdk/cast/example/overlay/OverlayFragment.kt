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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val directionButton: Button = view.findViewById(R.id.desiredInsertDirection)

        directionButton.setOnClickListener {
            if (directionButton.text == "Insertion Side:\nLeft") {

                directionButton.text = "Insertion Side:\nRight"
            } else {
                directionButton.text = "Insertion Side:\nLeft"
            }
        }

        val needleButton: LinearLayout = view.findViewById(R.id.btnNeedle)
        val needleVisibility: ImageView = view.findViewById(R.id.needleVisibility)
        val showDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.show)

        needleButton.setOnClickListener {
            val visibilityDrawable = needleVisibility.drawable;
            if (visibilityDrawable != null && showDrawable != null) {
                if (visibilityDrawable.constantState == showDrawable.constantState) {
                    needleVisibility.setImageResource(R.drawable.hide)
                    needleButton.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.purple
                        )
                    )
                } else {
                    needleVisibility.setImageResource(R.drawable.show)
                    needleButton.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                }
            }

        }

        val nerveButton: LinearLayout = view.findViewById(R.id.btnNerve)
        val nerveVisibility: ImageView = view.findViewById(R.id.nerveVisibility)
        val hideDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.hide)

        nerveButton.setOnClickListener {
            val visibilityDrawable = nerveVisibility.drawable;
            if (visibilityDrawable != null && hideDrawable != null) {
                if (visibilityDrawable.constantState == hideDrawable.constantState) {
                    nerveVisibility.setImageResource(R.drawable.show)
                    nerveButton.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.purple
                        )
                    )
                } else {
                    nerveVisibility.setImageResource(R.drawable.hide)
                    nerveButton.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                }

            }
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
        private var currentModelTag = "TAP"

        init {
            try {
                tfliteNerve = Interpreter(loadModelFile("TAP.tflite"))
                tfliteNeedle = Interpreter(loadModelFile("needle.tflite"))
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        private fun getModelFile(modelTag: String): String {
            return when (modelTag) {
                "Transabdominal Plane Block" -> "TAP.tflite"
                else -> "none"
            }
        }

        fun setNewModel(modelTag: String) {
            Log.d(TAG, "model tag: $modelTag")
            if (tfliteNerve != null) {
                tfliteNerve!!.close()
                Log.d(TAG, "Closed Current model resources")
            }
            val modelFile = getModelFile(modelTag)
            try {
                tfliteNerve = Interpreter((loadModelFile(modelFile)))
                currentModelTag = modelTag
                Log.d(TAG, "New model set to: $modelFile")
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
            val inputShape = tfliteNerve!!.getInputTensor(0).shape()
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
            val resizedBitmap = Bitmap.createScaledBitmap(inputImage, 512, 512, false)

            // Convert the Bitmap to a TensorImage
            val inputTensor = TensorImage(DataType.FLOAT32)
            inputTensor.load(resizedBitmap)
    
            // Output Buffers
            val outputBufferNerve = TensorBuffer.createFixedSize(intArrayOf(1, 512, 512, 1), DataType.FLOAT32)
            val outputBufferNeedle = TensorBuffer.createFixedSize(intArrayOf(1, 512, 512, 1), DataType.FLOAT32)
    
            // Run both models
            // TODO: Run models selectively based on UI
            tfliteNerve?.run(inputTensor.buffer, outputBufferNerve.buffer)
            tfliteNeedle?.run(inputTensor.buffer, outputBufferNeedle.buffer)
    
            // Process and overlay both outputs
            return postprocessOutput(outputBufferNerve, outputBufferNeedle, inputImage)
        }
    
        private fun postprocessOutput(outputNerveBuffer: TensorBuffer, outputNeedleBuffer: TensorBuffer, originalImage: Bitmap): Bitmap {
            val maskWidth = 512
            val maskHeight = 512
            val originalWidth = originalImage.width
            val originalHeight = originalImage.height
    
            // Create bitmaps for the masks
            val nerveBitmap = Bitmap.createBitmap(maskWidth, maskHeight, Bitmap.Config.ARGB_8888)
            val needleBitmap = Bitmap.createBitmap(maskWidth, maskHeight, Bitmap.Config.ARGB_8888)
    
            // Define mask colors
            val semiTransparentYellow = Color.argb(128, 255, 255, 0) // Nerve
            val semiTransparentRed = Color.argb(128, 255, 0, 0) // Needle
    
            for (x in 0 until maskWidth) {
                for (y in 0 until maskHeight) {
                    val nerveValue = outputNerveBuffer.getFloatValue(y * maskWidth + x)
                    val needleValue = outputNeedleBuffer.getFloatValue(y * maskWidth + x)
    
                    nerveBitmap.setPixel(x, y, if (nerveValue > 0.5) semiTransparentYellow else Color.TRANSPARENT)
                    needleBitmap.setPixel(x, y, if (needleValue > 0.5) semiTransparentRed else Color.TRANSPARENT)
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