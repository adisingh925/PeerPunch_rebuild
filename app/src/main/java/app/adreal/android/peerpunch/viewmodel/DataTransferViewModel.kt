package app.adreal.android.peerpunch.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import app.adreal.android.peerpunch.database.Database
import app.adreal.android.peerpunch.model.Data
import app.adreal.android.peerpunch.repository.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataTransferViewModel(application: Application) : AndroidViewModel(application) {

    private val readAllData : LiveData<List<Data>>

    private val repository: Repository

    init {
        val dao = Database.getDatabase(application).dao()
        repository = Repository(dao)
        readAllData = repository.readAllData
    }

    fun getAllData() : LiveData<List<Data>> {
        return readAllData
    }

    fun addData(data: Data){
        CoroutineScope(Dispatchers.IO).launch {
            repository.insertData(data)
        }
    }
}