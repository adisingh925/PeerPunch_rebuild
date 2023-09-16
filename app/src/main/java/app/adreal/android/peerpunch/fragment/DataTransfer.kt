package app.adreal.android.peerpunch.fragment

import android.annotation.SuppressLint
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

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        initRecycler()
        UDPSender.configureKeepAliveTimer()
        ((activity) as MainActivity).updateStatusBarColor(
            resources.getString(R.color.defaultBackground),
            "#00252e"
        )
        binding.toolbar.title = IPHandler.receiverIP.value + " : " + IPHandler.receiverPort.value
        binding.toolbar.subtitle = "Connected"

        binding.toolbar.setNavigationOnClickListener {
            Log.d("DataTransfer", "Toolbar Back pressed")
            UDPReceiver.setHasPeerExited(true)
        }

        ConnectionHandler.getConnectionStatus().observe(viewLifecycleOwner) {
            when (it) {
                Constants.getConnecting() -> {
                    binding.toolbar.subtitle = "Connecting"
                }

                Constants.getConnected() -> {
                    binding.toolbar.subtitle = "Connected"
                }

                Constants.getDisconnected() -> {
                    binding.toolbar.subtitle = "Disconnected"
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            Log.d("DataTransfer", "Back pressed")
            UDPReceiver.setHasPeerExited(true)
        }

        UDPSender.timeLeft.observe(viewLifecycleOwner) {
            if ((System.currentTimeMillis() - UDPReceiver.lastReceiveTime) > 2000) {
                ConnectionHandler.setConnectionStatus(Constants.getConnecting())
            }

            if ((System.currentTimeMillis() - UDPReceiver.lastReceiveTime) > 5000) {
                ConnectionHandler.setConnectionStatus(Constants.getDisconnected())
                UDPReceiver.setHasPeerExited(true)
            }
        }

        UDPReceiver.getHasPeerExited().observe(viewLifecycleOwner) {
            if (it) {
                Log.d("DataTransfer", "Terminating Connection")
                UDPSender.sendUDPMessage(Constants.getExitChatString())
                UDPSender.keepAliveTimer.cancel()
                Log.d("Navigating", "Navigating from DataTransfer to Home")
                ((activity) as MainActivity).updateStatusBarColor(
                    resources.getString(R.color.androidDefaultDark),
                    resources.getString(R.color.androidDefaultDark)
                )
                findNavController().popBackStack()
            }
        }

        binding.send.setOnClickListener {
            if (binding.messageInput.text.toString().isNotBlank()) {
                UDPSender.sendUDPMessage(binding.messageInput.text.toString())

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
        ConnectionHandler.setConnectionStatus(Constants.getConnecting())
        UDPSender.keepAliveTimer.start()
        UDPReceiver.lastReceiveTime = System.currentTimeMillis()
    }
}