package app.adreal.android.peerpunch.network

import android.util.Log
import app.adreal.android.peerpunch.util.Constants
import java.net.DatagramSocket

class SocketHandler {

    companion object {
        lateinit var UDPSocket: DatagramSocket

        fun initUDPSocket() {
            try {
                UDPSocket = DatagramSocket(Constants.getUdpPort())
            } catch (e: Exception) {
                Log.e("SocketHandler", "Error creating UDP socket: ${e.message}")
            }
        }
    }
}