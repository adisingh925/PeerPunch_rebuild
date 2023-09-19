package app.adreal.android.peerpunch.network

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object TCPServer {
    fun startTCPServer() {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.IO) {
                try {
                    SocketHandler.TCPServerSocket.accept()
                } catch (e: Exception) {
                    Log.e("TCPServer", "Error in accepting connection: ${e.message}")
                }
            }
        }
    }
}