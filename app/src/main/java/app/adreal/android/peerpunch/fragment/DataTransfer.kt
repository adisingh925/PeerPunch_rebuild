package app.adreal.android.peerpunch.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import app.adreal.android.peerpunch.adapter.ChatAdapter
import app.adreal.android.peerpunch.databinding.FragmentDataTransferBinding
import app.adreal.android.peerpunch.model.Data
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        initRecycler()

        UDPReceiver.lastReceiveTime = 0

        UDPSender.configureKeepAliveTimer()

        UDPSender.keepAliveTimer.start()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            UDPReceiver.setHasPeerExited(true)
        }

        UDPSender.timeLeft.observe(viewLifecycleOwner){
            if(UDPReceiver.lastReceiveTime != 0.toLong()){
                if((System.currentTimeMillis() - UDPReceiver.lastReceiveTime) > 5000){
                    UDPReceiver.setHasPeerExited(true)
                }
            }else{
                UDPReceiver.lastReceiveTime = System.currentTimeMillis()
            }
        }

        UDPReceiver.getHasPeerExited().observe(viewLifecycleOwner) {
            if(it){
                UDPSender.sendUDPMessage(Constants.getExitChatString())
                UDPSender.keepAliveTimer.cancel()
                findNavController().popBackStack()
            }
        }

        binding.send.setOnClickListener {
            if(binding.messageInput.text.toString().isNotBlank()){
                UDPSender.sendUDPMessage(binding.messageInput.text.toString())

                if(binding.messageInput.text.toString() == Constants.getExitChatString()){
                    UDPReceiver.setHasPeerExited(true)
                }else{
                    dataTransferViewModel.addData(Data(System.currentTimeMillis(), binding.messageInput.text.toString(), 0))
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
}