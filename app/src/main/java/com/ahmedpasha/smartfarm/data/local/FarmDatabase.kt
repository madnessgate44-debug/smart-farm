package com.ahmedpasha.smartfarm.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ahmedpasha.smartfarm.data.models.*

@Database(
    entities = [
        Land::class, Crop::class, Operation::class,
        InventoryItem::class, InventoryMovement::class,
        Animal::class, AnimalProduction::class,
        Worker::class, Attendance::class,
        Contact::class, Equipment::class, Maintenance::class,
        WaterLog::class, Purchase::class, Sale::class,
        TreasuryTransaction::class, Debt::class,
        Meeting::class, AhmedPreference::class, FarmTask::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FarmDatabase : RoomDatabase() {
    abstract fun farmDao(): FarmDao

    companion object {
        @Volatile
        private var INSTANCE: FarmDatabase? = null

        fun getDatabase(context: Context): FarmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FarmDatabase::class.java,
                    "smart_farm_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}