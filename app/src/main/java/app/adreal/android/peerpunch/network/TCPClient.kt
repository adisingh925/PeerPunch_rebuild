package app.adreal.android.peerpunch.network

import android.util.Log
import androidx.lifecycle.MutableLiveData
import app.adreal.android.peerpunch.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress

object TCPClient {

    val isTCPConnected = MutableLiveData(false)
    fun startTCPClient() {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.IO) {
                try {
                    SocketHandler.TCPSocket.connect(
                        InetSocketAddress(
                            IPHandler.receiverIP.value,
                            Constants.TCP_PORT
                        )
                    )

                    isTCPConnected.postValue(true)
                }catch (e: Exception) {
                    Log.e("TCPClient", "Error in connecting to server: ${e.message}")
                }
            }
        }
    }

    fun initTCPInputStream(){
        CoroutineScope(Dispatchers.IO).launch {
            SocketHandler.TCPInputStream = DataInputStream(withContext(Dispatchers.IO) {
                SocketHandler.TCPSocket.getInputStream()
            })
        }
    }

    fun initTCPOutputStream(){
        CoroutineScope(Dispatchers.IO).launch {
            SocketHandler.TCPOutputStream = DataOutputStream(withContext(Dispatchers.IO) {
                SocketHandler.TCPSocket.getOutputStream()
            })
        }
    }

    fun startTCPReceiver(){
        CoroutineScope(Dispatchers.IO).launch {
            val data = java.lang.StringBuilder()

            while (true) {
                // Wait for the STUN response
                val response = ByteArray(256)
                val byteRead = withContext(Dispatchers.IO) {
                    SocketHandler.TCPInputStream.read(response)
                }

                if (byteRead < response.size) {
                    data.append(String(response, 0, byteRead))
                    Log.d("TCPClient", "Received: $data")
                    data.clear()
                } else {
                    data.append(String(response, 0, byteRead))
                }
            }
        }
    }

    fun sendTCPData(data: String){
        CoroutineScope(Dispatchers.IO).launch {
            val chunks = data.chunked(256)
            for (i in chunks) {
                withContext(Dispatchers.IO) {
                    SocketHandler.TCPOutputStream.write(i.toByteArray())
                }
            }

            withContext(Dispatchers.IO) {
                SocketHandler.TCPOutputStream.flush()
            }
        }
    }
}