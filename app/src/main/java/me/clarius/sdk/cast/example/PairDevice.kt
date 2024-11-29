package me.clarius.sdk.cast.example

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import me.clarius.sdk.Cast
import me.clarius.sdk.ProbeInfo
import me.clarius.sdk.cast.example.CastService.CastBinder
import me.clarius.sdk.cast.example.databinding.FragmentPairBinding
import me.clarius.sdk.cast.example.overlay.CastWrapper
import java.nio.charset.StandardCharsets
import java.util.Optional

class PairDevice : Fragment() {
    private var binding: FragmentPairBinding? = null
    private var castBinder: CastBinder? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPairBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding!!.buttonConnect.setOnClickListener { _: View? -> doConnect() }
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
        if (!CastWrapper.isInitialized()) {
            CastWrapper.setCastService(CastService())
        }
        castBinder = CastWrapper.getCastService().CastBinder()
    }

    override fun onDestroy() {
        super.onDestroy()
//        val context = requireContext()
//        context.unbindService(castConnection)
        castBinder = null
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
                PairDevice.TAG,
                "Connection result: $result"
            )
            if (result) {
                Log.d(PairDevice.TAG, "UDP stream will be on port $port")
                Log.d(
                    PairDevice.TAG,
                    "App software " + (if (swRevMatch) "matches" else "does not match")
                )
                PairDevice.askProbeInfo(castBinder!!.getCast()!!)
            }
        }
    }

    private fun doDisconnect() {
        if (castBinder == null) {
            return
        }
        castBinder!!.getCast()!!.disconnect { result: Boolean ->
            Log.d(
                me.clarius.sdk.cast.example.PairDevice.TAG,
                "Disconnection result: $result"
            )
        }
    }

    private fun showError(text: CharSequence?) {
        Log.e(PairDevice.TAG, "Error: $text")
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post { Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show() }
    }

    private fun showMessage(text: CharSequence) {
        Log.d(PairDevice.TAG, (text as String))
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post { Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show() }
    }

    private val certificate: String
        get() = "research"

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