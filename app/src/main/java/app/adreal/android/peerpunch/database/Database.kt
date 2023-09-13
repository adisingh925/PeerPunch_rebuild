package app.adreal.android.peerpunch.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import app.adreal.android.peerpunch.dao.Dao
import app.adreal.android.peerpunch.model.Data

@androidx.room.Database(entities = [Data::class], version = 1, exportSchema = false)
abstract class Database : RoomDatabase() {

    abstract fun dao(): Dao

    companion object {

        @Volatile
        private var INSTANCE: Database? = null

        fun getDatabase(context: Context): Database {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this)
            {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    Database::class.java,
                    "UDPDatabase"
                ).build()

                INSTANCE = instance
                return instance
            }
        }
    }
}