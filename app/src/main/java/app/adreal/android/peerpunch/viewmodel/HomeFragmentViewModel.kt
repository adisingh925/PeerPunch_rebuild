package app.adreal.android.peerpunch.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeFragmentViewModel : ViewModel() {

    private val ip : MutableLiveData<String> = MutableLiveData()

    fun setIp(ip : String){
        this.ip.postValue(ip)
    }

    fun getIp() : MutableLiveData<String>{
        return ip
    }
}