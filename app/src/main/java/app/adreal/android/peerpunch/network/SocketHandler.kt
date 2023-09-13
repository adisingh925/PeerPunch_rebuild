package app.adreal.android.peerpunch.network

import android.util.Log
import java.net.DatagramSocket

class SocketHandler {

    companion object {
        private const val UDPPort = 50001
        lateinit var UDPSocket: DatagramSocket

        fun initUDPSocket() {
            try {
                UDPSocket = DatagramSocket(UDPPort)
            } catch (e: Exception) {
                Log.e("SocketHandler", "Error creating UDP socket: ${e.message}")
            }
        }
    }
}