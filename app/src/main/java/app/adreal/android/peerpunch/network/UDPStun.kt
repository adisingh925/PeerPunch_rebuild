package app.adreal.android.peerpunch.network

import de.javawi.jstun.attribute.ChangeRequest
import de.javawi.jstun.header.MessageHeader
import de.javawi.jstun.header.MessageHeaderInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.InetAddress

class UDPStun {

    companion object {
        private const val STUNTMAN_STUN_SERVER = "stunserver.stunprotocol.org"
        private const val STUNTMAN_STUN_SERVER_PORT = 3478
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
                InetAddress.getByName(STUNTMAN_STUN_SERVER),
                STUNTMAN_STUN_SERVER_PORT
            )

            SocketHandler.UDPSocket.send(datagramPacket)
        }
    }
}