package com.ahmedpasha.smartfarm

import android.app.Application
import com.ahmedpasha.smartfarm.data.local.FarmDatabase
import com.ahmedpasha.smartfarm.data.repository.FarmRepository

class FarmApplication : Application() {
    val database by lazy { FarmDatabase.getDatabase(this) }
    val repository by lazy { FarmRepository(database.farmDao()) }
}