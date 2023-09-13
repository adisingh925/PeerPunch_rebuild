package app.adreal.android.peerpunch.network

import java.net.DatagramPacket
import java.net.InetAddress

class UDPSender {

    companion object {
        fun sendUDPMessage(message: String) {
            val byteArrayData = message.toByteArray()
            val datagramPacket = DatagramPacket(
                byteArrayData,
                byteArrayData.size,
                InetAddress.getByName(IPHandler.publicIP.value),
                SocketHandler.UDPSocket.port
            )
            SocketHandler.UDPSocket.send(datagramPacket)
        }
    }
}