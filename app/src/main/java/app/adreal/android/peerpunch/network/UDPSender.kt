package app.adreal.android.peerpunch.network

import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import app.adreal.android.peerpunch.encryption.Encryption
import app.adreal.android.peerpunch.model.CipherDataSend
import app.adreal.android.peerpunch.model.Data
import app.adreal.android.peerpunch.model.ECDHPublicSend
import app.adreal.android.peerpunch.model.EncryptedData
import app.adreal.android.peerpunch.storage.SharedPreferences
import app.adreal.android.peerpunch.util.Constants
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.InetAddress
import java.util.Base64

class UDPSender {

    companion object {

        lateinit var keepAliveTimer: CountDownTimer
        lateinit var ECDHTimer: CountDownTimer

        val timeLeft = MutableLiveData<Long>()
        var ECDHTimeLeft = MutableLiveData<Long>()

        private var isECDHTimerFinished = MutableLiveData(false)

        fun getIsECDHTimerFinished(): MutableLiveData<Boolean> {
            return isECDHTimerFinished
        }

        fun setIsECDHTimerFinished(value: Boolean) {
            isECDHTimerFinished.postValue(value)
        }

        fun configureKeepAliveTimer(ip: String, port: Int) {
            keepAliveTimer = object : CountDownTimer(3600000, 500) {
                override fun onTick(millisUntilFinished: Long) {
                    sendUDPMessage(Constants.getConnectionEstablishString(), ip, port)
                    timeLeft.postValue(millisUntilFinished)
                }

                override fun onFinish() {
                    Log.d("UDPSender", "Keep alive timer finished")
                    UDPReceiver.setHasPeerExited(true)
                }
            }
        }

        fun configureECDHTimer(ip: String, port: Int) {
            ECDHTimer = object : CountDownTimer(5000, 500) {
                override fun onTick(millisUntilFinished: Long) {
                    Log.d(
                        "UDPSender",
                        "Sending ECDH public key : ${
                            SharedPreferences.read(
                                Encryption.ECDH_PUBLIC,
                                ""
                            )
                        }"
                    )

                    sendUDPMessage(
                        Gson().toJson(
                            ECDHPublicSend(
                                SharedPreferences.read(
                                    Encryption.ECDH_PUBLIC, ""
                                ).toString()
                            )
                        ).toByteArray(), ip, port
                    )

                    ECDHTimeLeft.postValue(millisUntilFinished)
                }

                override fun onFinish() {
                    isECDHTimerFinished.postValue(true)
                }
            }
        }

        fun sendUDPMessage(message: String, ip: String, port: Int) {
            CoroutineScope(Dispatchers.IO).launch {

                val chunks = message.chunked(256)

                for (chunk in chunks) {
                    val encryptedData = Encryption.encryptUsingSymmetricKey(chunk)
                    val byteArrayData = Gson().toJson(
                        CipherDataSend(
                            Base64.getEncoder().encodeToString(encryptedData.cipherText),
                            Base64.getEncoder().encodeToString(encryptedData.iv),
                            Encryption.generateHMAC(chunk)
                        )
                    ).toByteArray()

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

                    Log.d("Packet Size", byteArrayData.size.toString())
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