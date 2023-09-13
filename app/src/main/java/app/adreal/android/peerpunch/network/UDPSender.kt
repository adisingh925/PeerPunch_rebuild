package app.adreal.android.peerpunch.network

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.InetAddress

class UDPSender {

    companion object {

        lateinit var keepAliveTimer : CountDownTimer
        val timeLeft = MutableLiveData<Long>()

        fun configureKeepAliveTimer(){

            keepAliveTimer = object : CountDownTimer(3600000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    sendUDPMessage(UDPReceiver.CONNECTION_ESTABLISH_STRING)
                    timeLeft.postValue(millisUntilFinished)
                }

                override fun onFinish() {
                    UDPReceiver.setHasPeerExited(true)
                    sendUDPMessage(UDPReceiver.EXIT_CHAT)
                }
            }
        }

        fun sendUDPMessage(message: String) {
            CoroutineScope(Dispatchers.IO).launch {
                val byteArrayData = message.toByteArray()
                val datagramPacket = DatagramPacket(
                    byteArrayData,
                    byteArrayData.size,
                    withContext(Dispatchers.IO) {
                        InetAddress.getByName(IPHandler.receiverIP.value)
                    },
                    SocketHandler.UDPPort
                )
                withContext(Dispatchers.IO) {
                    SocketHandler.UDPSocket.send(datagramPacket)
                }
            }
        }
    }
}