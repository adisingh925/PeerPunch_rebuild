package app.adreal.android.peerpunch.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Data")
data class Data(
    @PrimaryKey
    val messageId: Long,
    val message : String
)
