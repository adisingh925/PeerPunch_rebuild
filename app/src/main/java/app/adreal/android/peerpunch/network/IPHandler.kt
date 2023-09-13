package app.adreal.android.peerpunch.network

import androidx.lifecycle.MutableLiveData

class IPHandler {

    companion object{
        val publicIP = MutableLiveData<String>()
    }
}