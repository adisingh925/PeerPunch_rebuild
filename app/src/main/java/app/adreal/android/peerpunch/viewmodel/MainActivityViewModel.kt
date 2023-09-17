package app.adreal.android.peerpunch.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import app.adreal.android.peerpunch.encryption.Encryption
import app.adreal.android.peerpunch.network.IPHandler
import app.adreal.android.peerpunch.network.SocketHandler

class MainActivityViewModel : ViewModel() {
    fun intiStartupClasses(context: Context) {
        IPHandler.privateIP.postValue(IPHandler.getIPAddress())
        SocketHandler.initUDPSocket(context)
    }
}