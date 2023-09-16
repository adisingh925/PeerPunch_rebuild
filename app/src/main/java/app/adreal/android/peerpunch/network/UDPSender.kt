package app.adreal.android.peerpunch.network

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.MutableLiveData
import app.adreal.android.peerpunch.model.Data
import app.adreal.android.peerpunch.util.Constants
import com.google.gson.Gson
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

        fun configureKeepAliveTimer(ip: String, port: Int) {

            keepAliveTimer = object : CountDownTimer(3600000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    sendUDPMessage(Constants.getConnectionEstablishString(), ip, port)
                    timeLeft.postValue(millisUntilFinished)
                }

                override fun onFinish() {
                    UDPReceiver.setHasPeerExited(true)
                }
            }
        }

        fun sendUDPMessage(message: String, ip: String, port: Int) {
            CoroutineScope(Dispatchers.IO).launch {

                val chunks = message.chunked(256)

                for (chunk in chunks) {

                    val byteArrayData = Gson().toJson(Data(System.currentTimeMillis(), chunk, 1)).toByteArray()

                    val datagramPacket = DatagramPacket(
                        byteArrayData,
                        byteArrayData.size,
                        withContext(Dispatchers.IO) {
                            InetAddress.getByName(ip)
                        },
                        port
                    )

                    withContext(Dispatchers.IO) {
                        try {
                            SocketHandler.UDPSocket.send(datagramPacket)
                        } catch (e: Exception) {
                            Log.e("UDPSender", "Error sending UDP message: ${e.message}")
                        }
                    }
                }
            }
        }

        fun sendUDPMessage(message: ByteArray, ip: String, port: Int) {
            CoroutineScope(Dispatchers.IO).launch {
                val datagramPacket = DatagramPacket(
                    message,
                    message.size,
                    withContext(Dispatchers.IO) {
                        InetAddress.getByName(ip)
                    },
                    port
                )
                withContext(Dispatchers.IO) {
                    try {
                        SocketHandler.UDPSocket.send(datagramPacket)
                    } catch (e: Exception) {
                        Log.e("UDPSender", "Error sending STUN request: ${e.message}")
                    }
                }
            }
        }
    }
}