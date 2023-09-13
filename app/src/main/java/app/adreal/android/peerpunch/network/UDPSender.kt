package app.adreal.android.peerpunch.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.InetAddress

class UDPSender {

    companion object {
        fun sendUDPMessage(message: String) {
            CoroutineScope(Dispatchers.IO).launch {
                val byteArrayData = message.toByteArray()
                val datagramPacket = DatagramPacket(
                    byteArrayData,
                    byteArrayData.size,
                    InetAddress.getByName(IPHandler.publicIP.value),
                    SocketHandler.UDPPort
                )
                SocketHandler.UDPSocket.send(datagramPacket)
            }
        }
    }
}