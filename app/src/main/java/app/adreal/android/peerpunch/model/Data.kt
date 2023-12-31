package app.adreal.android.peerpunch.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Data")
data class Data(
    @PrimaryKey
    var messageId: Long,
    val message : String,
    val isReceived : Int = 0
)
