package app.adreal.android.peerpunch.network

import androidx.lifecycle.MutableLiveData

class ConnectionHandler
{
    companion object{
        private val connectionStatus = MutableLiveData<Int>()

        fun setConnectionStatus(status: Int){
            connectionStatus.postValue(status)
        }

        fun getConnectionStatus(): MutableLiveData<Int>{
            return connectionStatus
        }
    }
}