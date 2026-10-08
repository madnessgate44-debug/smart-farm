package com.ahmedpasha.smartfarm.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ahmedpasha.smartfarm.data.models.*

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add nullable columns to treasury_transactions
        db.execSQL("ALTER TABLE treasury_transactions ADD COLUMN sourceType TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE treasury_transactions ADD COLUMN sourceId INTEGER DEFAULT NULL")
        
        // Handle duplicate attendance records for unique index: keep first occurrence by id
        db.execSQL(
            "DELETE FROM attendance WHERE id NOT IN (" +
            "  SELECT MIN(id) FROM attendance GROUP BY workerCode, date" +
            ")"
        )
        
        // Create a new table with unique index, copy data, then rename
        db.execSQL(
            "CREATE TABLE attendance_new (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "workerCode TEXT NOT NULL, " +
            "date TEXT NOT NULL, " +
            "status TEXT NOT NULL, " +
            "delayHours REAL DEFAULT 0.0, " +
            "bonus REAL DEFAULT 0.0, " +
            "deduction REAL DEFAULT 0.0, " +
            "notes TEXT DEFAULT '', " +
            "UNIQUE(workerCode, date)" +
            ")"
        )
        db.execSQL(
            "INSERT INTO attendance_new (id, workerCode, date, status, delayHours, bonus, deduction, notes) " +
            "SELECT id, workerCode, date, status, delayHours, bonus, deduction, notes FROM attendance"
        )
        db.execSQL("DROP TABLE attendance")
        db.execSQL("ALTER TABLE attendance_new RENAME TO attendance")
    }
}

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
    version = 2,
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
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
