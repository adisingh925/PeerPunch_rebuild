package app.adreal.android.peerpunch.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import app.adreal.android.peerpunch.adapter.ChatAdapter
import app.adreal.android.peerpunch.databinding.FragmentDataTransferBinding
import app.adreal.android.peerpunch.model.Data
import app.adreal.android.peerpunch.network.UDPSender
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

        binding.send.setOnClickListener {
            if(binding.messageInput.text.toString().isNotBlank()){
                dataTransferViewModel.addData(Data(System.currentTimeMillis(), binding.messageInput.text.toString(), 0))
                UDPSender.sendUDPMessage(binding.messageInput.text.toString())
                binding.messageInput.setText("")
            }
        }

        dataTransferViewModel.getAllData().observe(viewLifecycleOwner) {
            adapter.setData(it)
            recyclerView.smoothScrollToPosition(adapter.itemCount)
        }

        recyclerView.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom < oldBottom) {
                recyclerView.post {
                    recyclerView.smoothScrollToPosition(0)
                }
            }
        }

        return binding.root
    }

    private fun initRecycler() {
        recyclerView.adapter = adapter
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        linearLayoutManager.isSmoothScrollbarEnabled = true
        recyclerView.layoutManager = linearLayoutManager
    }
}