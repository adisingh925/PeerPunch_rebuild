package app.adreal.android.peerpunch.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.adreal.android.peerpunch.model.Data

@Dao
interface Dao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addData(data: Data)

    @Query("SELECT * from Data")
    fun readAllData(): LiveData<List<Data>>
}