package app.adreal.android.peerpunch.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.adreal.android.peerpunch.database.Database
import app.adreal.android.peerpunch.model.Data
import app.adreal.android.peerpunch.repository.Repository

class HomeFragmentViewModel : ViewModel() {

    private val ip : MutableLiveData<String> = MutableLiveData()

    fun setIp(ip : String){
        this.ip.postValue(ip)
    }

    fun getIp() : MutableLiveData<String>{
        return ip
    }
}