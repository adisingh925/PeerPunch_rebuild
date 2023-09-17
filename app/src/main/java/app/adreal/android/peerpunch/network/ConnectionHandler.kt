package app.adreal.android.peerpunch.network

import androidx.lifecycle.MutableLiveData
import app.adreal.android.peerpunch.util.Constants

object ConnectionHandler {
    private val connectionStatus = MutableLiveData(Constants.getDisconnected())

    fun setConnectionStatus(status: Int) {
        connectionStatus.postValue(status)
    }

    fun getConnectionStatus(): MutableLiveData<Int> {
        return connectionStatus
    }
}
