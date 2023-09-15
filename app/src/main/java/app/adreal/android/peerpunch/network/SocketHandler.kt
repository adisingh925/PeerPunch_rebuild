package app.adreal.android.peerpunch.network

import android.content.Context
import android.util.Log
import app.adreal.android.peerpunch.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.DatagramSocket

class SocketHandler {

    companion object {
        lateinit var UDPSocket: DatagramSocket

        fun initUDPSocket(context: Context) {
            try {
                UDPSocket = DatagramSocket(Constants.getUdpPort())
                if (this::UDPSocket.isInitialized) {
                    UDPReceiver.startUDPReceiver(context)
                    UDPStun.sendUDPBindingRequest()
                }
            } catch (e: Exception) {
                Log.e("SocketHandler", "Error creating UDP socket: ${e.message}")
            }
        }
    }
}