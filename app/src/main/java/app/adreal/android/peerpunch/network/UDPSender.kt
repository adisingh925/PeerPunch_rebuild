package app.adreal.android.peerpunch.network

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.MutableLiveData
import app.adreal.android.peerpunch.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.InetAddress

class UDPSender {

    companion object {

        lateinit var keepAliveTimer: CountDownTimer
        val timeLeft = MutableLiveData<Long>()

        fun configureKeepAliveTimer() {

            keepAliveTimer = object : CountDownTimer(3600000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    sendUDPMessage(Constants.getConnectionEstablishString())
                    timeLeft.postValue(millisUntilFinished)
                }

                override fun onFinish() {
                    UDPReceiver.setHasPeerExited(true)
                }
            }
        }

        fun sendUDPMessage(message: String) {
            CoroutineScope(Dispatchers.IO).launch {

                val chunks = message.chunked(256)

                for (chunk in chunks) {
                    val byteArrayData = chunk.toByteArray()

                    val datagramPacket = DatagramPacket(
                        byteArrayData,
                        byteArrayData.size,
                        withContext(Dispatchers.IO) {
                            InetAddress.getByName(IPHandler.receiverIP.value)
                        },
                        IPHandler.receiverPort.value!!
                    )
                    withContext(Dispatchers.IO) {
                        SocketHandler.UDPSocket.send(datagramPacket)
                    }
                }
            }
        }
    }
}