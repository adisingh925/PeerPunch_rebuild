package app.adreal.android.peerpunch.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.adreal.android.peerpunch.model.Data

@Dao
interface Dao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addData(data: Data)

    @Query("SELECT * from Data order by messageId ASC")
    fun readAllData(): LiveData<List<Data>>

    @Query("delete from Data")
    suspend fun deleteAllData()
}