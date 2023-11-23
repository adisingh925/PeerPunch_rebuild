package app.adreal.android.peerpunch.network

import android.content.Context
import android.net.InetAddresses
import android.util.Log
import app.adreal.android.peerpunch.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

object SocketHandler {

    lateinit var UDPSocket: ArrayList<DatagramSocket>
    lateinit var TCPSocket: Socket
    lateinit var TCPServerSocket: ServerSocket
    lateinit var TCPInputStream: DataInputStream
    lateinit var TCPOutputStream: DataOutputStream

    fun initSockets(context: Context) {
        try {
            initUDPClient(Constants.getUdpPort(), context)
            initTCPClient()
            initTCPServer()
        } catch (e: Exception) {
            Log.e("SocketHandler", "Error creating UDP socket: ${e.message}")
        }
    }

    private fun initUDPClient(port: Int, context: Context) {
        if (!this::UDPSocket.isInitialized) {
            UDPSocket = ArrayList()
        }

        val socket = DatagramSocket(port)
        UDPSocket.add(socket)
        UDPReceiver.startUDPReceiver(context, socket)
        UDPStun.sendUDPBindingRequest()
    }

    private fun initTCPClient() {
        TCPSocket = Socket()
        TCPSocket.reuseAddress = true
        TCPSocket.bind(InetSocketAddress(Constants.getTcpPort()))
        Log.d("SocketHandler", "TCPSocket bound to port ${TCPSocket.localPort}")
    }

    private fun initTCPServer() {
        TCPServerSocket = ServerSocket()
        TCPServerSocket.reuseAddress = true
        TCPServerSocket.bind(InetSocketAddress(Constants.getTcpPort()))
        Log.d("SocketHandler", "TCPServerSocket bound to port ${TCPServerSocket.localPort}")
    }
}
