package app.adreal.android.peerpunch.network

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import app.adreal.android.peerpunch.database.Database
import app.adreal.android.peerpunch.encryption.Encryption
import app.adreal.android.peerpunch.model.CipherDataSend
import app.adreal.android.peerpunch.model.Data
import app.adreal.android.peerpunch.model.ECDHPublicSend
import app.adreal.android.peerpunch.model.TCPCredentialsSend
import app.adreal.android.peerpunch.util.Constants
import com.google.gson.Gson
import de.javawi.jstun.attribute.MappedAddress
import de.javawi.jstun.attribute.MessageAttributeInterface
import de.javawi.jstun.header.MessageHeader
import de.javawi.jstun.header.MessageHeaderInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.util.Base64

object UDPReceiver {

    private val hasPeerExited = MutableLiveData(false)
    private var isECDHReceived = MutableLiveData(false)
    private var isAESKeyGenerated = MutableLiveData(false)
    private var isTCPCredentialsReceived = MutableLiveData(false)
    var lastReceiveTime: Long = 0

    fun getHasPeerExited(): MutableLiveData<Boolean> {
        return hasPeerExited
    }

    fun setHasPeerExited(value: Boolean) {
        hasPeerExited.postValue(value)
    }

    fun getIsECDHReceived(): MutableLiveData<Boolean> {
        return isECDHReceived
    }

    fun getIsAESKeyGenerated(): MutableLiveData<Boolean> {
        return isAESKeyGenerated
    }

    fun setIsAESKeyGenerated(value: Boolean) {
        isAESKeyGenerated.postValue(value)
    }

    fun setIsECDHReceived(value: Boolean) {
        isECDHReceived.postValue(value)
    }

    fun getIsTCPCredentialsReceived(): MutableLiveData<Boolean> {
        return isTCPCredentialsReceived
    }

    fun setIsTCPCredentialsReceived(value: Boolean) {
        isTCPCredentialsReceived.postValue(value)
    }

    fun startUDPReceiver(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                val datagramPacket = DatagramPacket(ByteArray(512), 512)
                withContext(Dispatchers.IO) {
                    SocketHandler.UDPSocket.receive(datagramPacket)
                }

                val messageType = ((datagramPacket.data[0].toInt() shl 8) or datagramPacket.data[1].toInt()).toShort()

                if (messageType == 0x0101.toShort()) {
                    Log.d("UDPReceiver", "Received UDP binding response")
                    try {
                        val receiveMH = MessageHeader(MessageHeaderInterface.MessageHeaderType.BindingRequest)
                        receiveMH.parseAttributes(datagramPacket.data)
                        val mappedAddress = receiveMH.getMessageAttribute(MessageAttributeInterface.MessageAttributeType.MappedAddress) as MappedAddress

                        IPHandler.publicIP.postValue(mappedAddress.address.toString())
                        IPHandler.publicPort.postValue(mappedAddress.port)
                    } catch (e: Exception) {
                        Log.d("UDPReceiver", "Error parsing UDP binding packet: ${e.message}")
                    }
                } else {
                    try {
                        val receivedData = String(datagramPacket.data, 0, datagramPacket.data.indexOf(0))
                        Log.d("UDPReceiver", "Received data: $receivedData")

                        if (isECDHReceived.value == false && receivedData.contains("publicKey")) {
                            try {
                                isECDHReceived.postValue(true)
                                val parsedData = Gson().fromJson(receivedData, ECDHPublicSend::class.java)
                                Encryption.generateECDHSecret(parsedData.publicKey)
                            } catch (e: Exception) {
                                Log.d("UDPReceiver", "Error parsing ECDH packet: ${e.message}")
                            } finally {
                                setIsAESKeyGenerated(true)
                            }
                        } else {
                            if (!Encryption.isSymmetricKeyEmpty()) {
                                if(receivedData.contains("cipherText")){
                                    val parsedCipherData = Gson().fromJson(receivedData, CipherDataSend::class.java)

                                    val message = Encryption.decryptUsingSymmetricEncryption(
                                        Base64.getDecoder().decode(parsedCipherData.cipherText),
                                        Base64.getDecoder().decode(parsedCipherData.iv)
                                    )

                                    if (Encryption.compareMessageAndHMAC(
                                            message,
                                            parsedCipherData.hash
                                        )
                                    ) {
                                        Log.d("UDPReceiver", "HMAC Successfully Matched")
                                        if (message == Constants.getExitChatString()) {
                                            if (hasPeerExited.value == false) {
                                                Log.d("UDPReceiver", "Exit request received")
                                                setHasPeerExited(true)
                                            } else {
                                                Log.d(
                                                    "UDPReceiver",
                                                    "Exit request received And Ignored"
                                                )
                                            }
                                        } else if (message == Constants.getConnectionEstablishString()) {
                                            if (hasPeerExited.value == false) {
                                                lastReceiveTime = System.currentTimeMillis()
                                                Log.d("UDPReceiver", "Received keep alive message")
                                            } else {
                                                Log.d(
                                                    "UDPReceiver",
                                                    "Received keep alive message And Ignored"
                                                )
                                            }
                                        } else if (message.contains("clientPort") && message.contains("serverPort")) {
                                            if(getIsTCPCredentialsReceived().value == false){
                                                val parsedTCPData = Gson().fromJson(message, TCPCredentialsSend::class.java)
                                                IPHandler.TCPReceiverPortData.clientPort = parsedTCPData.clientPort
                                                IPHandler.TCPReceiverPortData.serverPort = parsedTCPData.serverPort
                                                setIsTCPCredentialsReceived(true)
                                                Log.d("UDPReceiver", "Received TCP credentials $parsedTCPData")
                                            }else{
                                                Log.d("UDPReceiver", "Received TCP credentials And Ignored")
                                            }
                                        } else {
                                            Log.d("UDPReceiver", "Message received from peer")
                                            Database.getDatabase(context).dao().addData(Data(System.currentTimeMillis(), message, 1))
                                        }
                                    }
                                }
                            } else {
                                Log.d("UDPReceiver", "Symmetric key is empty")
                            }
                        }
                    } catch (e: Exception) {
                        Log.d("UDPReceiver", "Some Error Has Occurred : ${e.message}")
                    }
                }
            }
        }
    }
}
