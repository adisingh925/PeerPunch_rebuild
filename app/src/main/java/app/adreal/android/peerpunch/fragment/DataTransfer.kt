package app.adreal.android.peerpunch.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import app.adreal.android.peerpunch.MainActivity
import app.adreal.android.peerpunch.R
import app.adreal.android.peerpunch.adapter.ChatAdapter
import app.adreal.android.peerpunch.databinding.FragmentDataTransferBinding
import app.adreal.android.peerpunch.model.Data
import app.adreal.android.peerpunch.network.ConnectionHandler
import app.adreal.android.peerpunch.network.IPHandler
import app.adreal.android.peerpunch.network.UDPReceiver
import app.adreal.android.peerpunch.network.UDPSender
import app.adreal.android.peerpunch.util.Constants
import app.adreal.android.peerpunch.viewmodel.DataTransferViewModel

class DataTransfer : Fragment() {

    private val binding by lazy {
        FragmentDataTransferBinding.inflate(layoutInflater)
    }

    private val dataTransferViewModel by lazy {
        ViewModelProvider(this)[DataTransferViewModel::class.java]
    }

    private val adapter by lazy {
        ChatAdapter(requireContext())
    }

    private val recyclerView by lazy {
        binding.recyclerView
    }

    private val linearLayoutManager by lazy {
        LinearLayoutManager(context)
    }

    private val receiverIP by lazy{
        IPHandler.receiverIP.value!!
    }

    private val receiverPORT by lazy{
        IPHandler.receiverPort.value!!
    }

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        initRecycler()
        UDPSender.configureECDHTimer(receiverIP, receiverPORT)

        ((activity) as MainActivity).updateStatusBarColor(
            resources.getString(R.color.defaultBackground),
            resources.getString(R.color.darkNavigationBarColor)
        )

        binding.toolbar.title = IPHandler.receiverIP.value + " : " + IPHandler.receiverPort.value

        binding.toolbar.setNavigationOnClickListener {
            Log.d("DataTransfer", "Toolbar Back pressed")
            UDPReceiver.setHasPeerExited(true)
        }

        UDPReceiver.getIsECDHReceived().observe(viewLifecycleOwner){
            if (it) {
                UDPReceiver.lastReceiveTime = (System.currentTimeMillis() - 3000)
                UDPSender.configureKeepAliveTimer(receiverIP, receiverPORT)
                UDPSender.keepAliveTimer.start()
            }
        }

        UDPSender.getIsECDHTimerFinished().observe(viewLifecycleOwner){
            if (it) {
                if(UDPReceiver.getIsECDHReceived().value == false){
                    UDPReceiver.setHasPeerExited(true)
                }
            }
        }

        ConnectionHandler.getConnectionStatus().observe(viewLifecycleOwner) {
            when (it) {
                Constants.getConnecting() -> {
                    binding.toolbar.setSubtitleTextColor(Color.parseColor(resources.getString(R.color.yellow)))
                    binding.toolbar.subtitle = "Connecting..."
                }

                Constants.getConnected() -> {
                    binding.toolbar.setSubtitleTextColor(Color.parseColor(resources.getString(R.color.green)))
                    binding.toolbar.subtitle = "Connected"
                }

                Constants.getDisconnected() -> {
                    binding.toolbar.setSubtitleTextColor(Color.parseColor(resources.getString(R.color.red)))
                    binding.toolbar.subtitle = "Disconnected"
                }

                Constants.getExchangingKeys() -> {
                    binding.toolbar.setSubtitleTextColor(Color.parseColor(resources.getString(R.color.yellow)))
                    binding.toolbar.subtitle = "Exchanging Keys..."
                }

                Constants.getGeneratingAesKey() -> {
                    binding.toolbar.setSubtitleTextColor(Color.parseColor(resources.getString(R.color.yellow)))
                    binding.toolbar.subtitle = "Generating AES Key..."
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            Log.d("DataTransfer", "Back pressed")
            UDPReceiver.setHasPeerExited(true)
        }

        UDPSender.timeLeft.observe(viewLifecycleOwner) {
            if(UDPReceiver.getIsECDHReceived().value == true){
                if ((System.currentTimeMillis() - UDPReceiver.lastReceiveTime) < 3000) {
                    if (ConnectionHandler.getConnectionStatus().value != Constants.getConnected()) {
                        ConnectionHandler.setConnectionStatus(Constants.getConnected())
                    }
                }

                if ((System.currentTimeMillis() - UDPReceiver.lastReceiveTime) >= 3000) {
                    if (ConnectionHandler.getConnectionStatus().value != Constants.getConnecting()) {
                        ConnectionHandler.setConnectionStatus(Constants.getConnecting())
                    }
                }

                if ((System.currentTimeMillis() - UDPReceiver.lastReceiveTime) > 10000) {
                    Log.d("DataTransfer", "Connection Timeout")
                    UDPReceiver.setHasPeerExited(true)
                }
            }
        }

        UDPReceiver.getHasPeerExited().observe(viewLifecycleOwner) {
            if (it) {
                Log.d("DataTransfer", "Terminating Connection")
                UDPSender.keepAliveTimer.cancel()
                UDPSender.ECDHTimer.cancel()
                UDPSender.sendUDPMessage(Constants.getExitChatString(), receiverIP, receiverPORT)
                ConnectionHandler.setConnectionStatus(Constants.getDisconnected())
                ((activity) as MainActivity).updateStatusBarColor(
                    resources.getString(R.color.androidDefaultDark),
                    resources.getString(R.color.androidDefaultDark)
                )
                Log.d("Navigating", "Navigating from DataTransfer to Home")
                dataTransferViewModel.deleteAllData()
                findNavController().popBackStack()
            }
        }

        binding.send.setOnClickListener {
            if (binding.messageInput.text.toString().isNotBlank()) {
                UDPSender.sendUDPMessage(binding.messageInput.text.toString(), receiverIP, receiverPORT)

                if (binding.messageInput.text.toString() == Constants.getExitChatString()) {
                    UDPReceiver.setHasPeerExited(true)
                } else {
                    dataTransferViewModel.addData(
                        Data(
                            System.currentTimeMillis(),
                            binding.messageInput.text.toString(),
                            0
                        )
                    )
                }

                binding.messageInput.setText("")
            }
        }

        dataTransferViewModel.getAllData().observe(viewLifecycleOwner) {
            adapter.setData(it)
            recyclerView.smoothScrollToPosition(adapter.itemCount)
        }

        recyclerView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                recyclerView.post {
                    recyclerView.smoothScrollToPosition(adapter.itemCount)
                }
            }
        }

        return binding.root
    }

    private fun initRecycler() {
        recyclerView.adapter = adapter
        linearLayoutManager.stackFromEnd = true
        linearLayoutManager.isSmoothScrollbarEnabled = true
        recyclerView.layoutManager = linearLayoutManager
    }

    override fun onStart() {
        super.onStart()
        ConnectionHandler.setConnectionStatus(Constants.getExchangingKeys())
        UDPSender.ECDHTimer.start()
    }
}