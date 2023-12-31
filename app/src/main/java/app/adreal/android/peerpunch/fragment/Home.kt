package app.adreal.android.peerpunch.fragment

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import app.adreal.android.peerpunch.R
import app.adreal.android.peerpunch.databinding.FragmentHomeBinding
import app.adreal.android.peerpunch.encryption.Encryption
import app.adreal.android.peerpunch.network.IPHandler
import app.adreal.android.peerpunch.network.TCPClient
import app.adreal.android.peerpunch.network.UDPReceiver
import app.adreal.android.peerpunch.network.UDPSender
import app.adreal.android.peerpunch.storage.SharedPreferences
import app.adreal.android.peerpunch.util.Constants
import app.adreal.android.peerpunch.viewmodel.HomeFragmentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Home : Fragment() {

    private val binding by lazy {
        FragmentHomeBinding.inflate(layoutInflater)
    }

    private val homeFragmentViewModel by lazy {
        ViewModelProvider(this)[HomeFragmentViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding.ipInput.setText(homeFragmentViewModel.getIp().value)

        binding.ipInput.doOnTextChanged { text, _, _, _ ->
            homeFragmentViewModel.setIp(text.toString())
        }

        IPHandler.publicIP.observe(viewLifecycleOwner) {
            binding.publicIP.text = it
        }

        IPHandler.publicPort.observe(viewLifecycleOwner) {
            binding.publicIP.text = buildString {
                append(binding.publicIP.text.toString())
                append(" : ")
                append(it.toString())
            }
        }

        IPHandler.privateIP.observe(viewLifecycleOwner) {
            binding.privateIP.text = it
        }

        binding.connect.setOnClickListener {
            var checkFailed = 0
            binding.ipLayout.error = null

            if (binding.portInput.text.toString().isNotBlank()) {
                IPHandler.receiverPort.postValue(binding.portInput.text.toString().toInt())
            } else {
                IPHandler.receiverPort.postValue(Constants.getUdpPort())
            }

            if (binding.ipInput.text.toString().isNotBlank()) {
                if (Patterns.IP_ADDRESS.matcher(binding.ipInput.text.toString()).matches()) {
                    IPHandler.receiverIP.postValue(binding.ipInput.text.toString())
                } else {
                    binding.ipLayout.error = "Invalid IP Address"
                    checkFailed++
                }
            } else {
                IPHandler.receiverIP.postValue(Constants.getLoopbackAddress())
            }

            if(checkFailed == 0){
                UDPReceiver.setHasPeerExited(false)
                UDPReceiver.setIsECDHReceived(false)
                UDPReceiver.setIsAESKeyGenerated(false)
                TCPClient.isTCPConnected.postValue(false)
                UDPReceiver.setIsTCPCredentialsReceived(false)
                IPHandler.TCPReceiverPortData.serverPort = 0
                IPHandler.TCPReceiverPortData.clientPort = 0
                UDPSender.timeLeft.postValue(-1)
                UDPReceiver.lastReceiveTime = 0
                Encryption.setSymmetricKey("")
                Log.d("Navigating","Navigating from Home to DataTransfer")
                findNavController().navigate(R.id.action_home2_to_dataTransfer)
            }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        Log.d("Home","onStart")
        CoroutineScope(Dispatchers.IO).launch {
            Encryption.addBouncyCastleProvider()
        }.invokeOnCompletion {
            Log.d("Encryption", "Keypair successfully generated")
        }
    }
}