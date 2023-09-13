package app.adreal.android.peerpunch.network

import de.javawi.jstun.attribute.ChangeRequest
import de.javawi.jstun.header.MessageHeader
import de.javawi.jstun.header.MessageHeaderInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.InetAddress

class UDPStun {

    companion object {
        private const val GOOGLE_STUN_SERVER_IP = "74.125.197.127"
        private const val GOOGLE_STUN_SERVER_PORT = 19302
    }

    fun sendUDPBindingRequest() {
        CoroutineScope(Dispatchers.IO).launch {
            val sendMH = MessageHeader(MessageHeaderInterface.MessageHeaderType.BindingRequest)
            val changeRequest = ChangeRequest()
            sendMH.addMessageAttribute(changeRequest)
            val data = sendMH.bytes

            val datagramPacket = DatagramPacket(
                data,
                data.size,
                withContext(Dispatchers.IO) {
                    InetAddress.getByName(GOOGLE_STUN_SERVER_IP)
                },
                GOOGLE_STUN_SERVER_PORT
            )

            withContext(Dispatchers.IO) {
                SocketHandler.UDPSocket.send(datagramPacket)
            }
        }
    }
}