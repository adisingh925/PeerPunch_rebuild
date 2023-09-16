package app.adreal.android.peerpunch.viewmodel

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import app.adreal.android.peerpunch.encryption.Encryption
import app.adreal.android.peerpunch.network.IPHandler
import app.adreal.android.peerpunch.network.SocketHandler

class MainActivityViewModel : ViewModel() {
    @RequiresApi(Build.VERSION_CODES.O)
    fun intiStartupClasses(context: Context) {
        IPHandler.privateIP.postValue(IPHandler.getIPAddress())
        Encryption.addBouncyCastleProvider()
        SocketHandler.initUDPSocket(context)
    }
}