package app.adreal.android.peerpunch.fragment

import android.net.InetAddresses
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import app.adreal.android.peerpunch.R
import app.adreal.android.peerpunch.databinding.FragmentHomeBinding
import app.adreal.android.peerpunch.network.IPHandler
import app.adreal.android.peerpunch.network.SocketHandler
import app.adreal.android.peerpunch.network.UDPReceiver
import app.adreal.android.peerpunch.viewmodel.HomeFragmentViewModel
import java.net.InetAddress
import java.util.regex.Pattern

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

        IPHandler.privateIP.observe(viewLifecycleOwner) {
            binding.privateIP.text = it
        }

        binding.connect.setOnClickListener {
            binding.ipLayout.error = null

            if (binding.ipInput.text.toString().isNotBlank()) {
                if (Patterns.IP_ADDRESS.matcher(binding.ipInput.text.toString()).matches()) {
                    IPHandler.publicIP.postValue(binding.ipInput.text.toString())
                    findNavController().navigate(R.id.action_home2_to_dataTransfer)
                } else {
                    binding.ipLayout.error = "Invalid IP Address"
                }
            } else {
                binding.ipLayout.error = "IP Address cannot be empty"
            }
        }

        return binding.root
    }
}