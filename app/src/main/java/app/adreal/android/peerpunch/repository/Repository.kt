package app.adreal.android.peerpunch.repository

import app.adreal.android.peerpunch.dao.Dao
import app.adreal.android.peerpunch.model.Data

class Repository(private val dao: Dao) {

    val readAllData = dao.readAllData()

    fun insertData(data: Data) {
        dao.addData(data)
    }
}