package app.adreal.android.peerpunch.network

import android.content.Context
import android.net.InetAddresses
import android.util.Log
import app.adreal.android.peerpunch.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

object SocketHandler {

    lateinit var UDPSocket: DatagramSocket
    lateinit var TCPSocket : Socket
    lateinit var TCPServerSocket : ServerSocket

    fun initSockets(context: Context) {
        try {
            initUDPClient()
//            initTCPClient()
//            initTCPServer()
            if (this::UDPSocket.isInitialized) {
                UDPReceiver.startUDPReceiver(context)
                UDPStun.sendUDPBindingRequest()
            }
        } catch (e: Exception) {
            Log.e("SocketHandler", "Error creating UDP socket: ${e.message}")
        }
    }

    private fun initUDPClient(){
        UDPSocket = DatagramSocket(Constants.getUdpPort())
    }

//    private fun initTCPClient(){
//        TCPSocket = Socket()
//        TCPSocket.reuseAddress = true
//        TCPSocket.bind(InetSocketAddress(Constants.getTcpPort()))
//    }
//
//    private fun initTCPServer(){
//        TCPServerSocket = ServerSocket()
//        TCPServerSocket.reuseAddress = true
//        TCPServerSocket.bind(InetSocketAddress(Constants.getTcpPort()))
//    }
}
