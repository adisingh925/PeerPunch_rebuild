package app.adreal.android.peerpunch.network

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.MutableLiveData
import app.adreal.android.peerpunch.database.Database
import app.adreal.android.peerpunch.model.Data
import app.adreal.android.peerpunch.util.Constants
import de.javawi.jstun.attribute.MappedAddress
import de.javawi.jstun.attribute.MessageAttributeInterface
import de.javawi.jstun.header.MessageHeader
import de.javawi.jstun.header.MessageHeaderInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramPacket

class UDPReceiver {

    companion object {

        private val hasPeerExited = MutableLiveData(true)
        var lastReceiveTime : Long = 0

        fun getHasPeerExited(): MutableLiveData<Boolean> {
            return hasPeerExited
        }

        fun setHasPeerExited(value: Boolean) {
            hasPeerExited.postValue(value)
        }

        fun startUDPReceiver(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                while (true) {
                    val datagramPacket = DatagramPacket(ByteArray(512), 512)
                    withContext(Dispatchers.IO) {
                        SocketHandler.UDPSocket.receive(datagramPacket)
                    }

                    val receivedData = String(datagramPacket.data, 0, datagramPacket.data.indexOf(0))

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
                    } else if (receivedData == Constants.getExitChatString()) {
                        if(hasPeerExited.value == false){
                            Log.d("UDPReceiver", "Exit request received")
                            hasPeerExited.postValue(true)
                        }else{
                            Log.d("UDPReceiver", "Exit request received And Ignored")
                        }
                    } else if (receivedData == Constants.getConnectionEstablishString()) {
                        if(hasPeerExited.value == false){
                            lastReceiveTime = System.currentTimeMillis()
                            Log.d("UDPReceiver", "Received keep alive message")
                        }else{
                            Log.d("UDPReceiver", "Received keep alive message And Ignored")
                        }
                    } else {
                        Log.d("UDPReceiver", "Message received from peer")
                        Database.getDatabase(context).dao().addData(Data(System.currentTimeMillis(), receivedData, 1))
                    }
                }
            }
        }
    }
}