package com.example.minecraftserverstatus.data.dbLocal

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ServerLocal::class], version = 1)
abstract class AppDataBase : RoomDatabase() {
    abstract fun serverDao(): ServerDAO

    companion object {
        @Volatile
        private var _instance: AppDataBase? = null

        fun getInstance(context: Context): AppDataBase =
            _instance ?: synchronized(this) {
                _instance ?: buildDatabase(context).also { _instance = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext,
                AppDataBase::class.java, "app_database")
                .fallbackToDestructiveMigration()
                .build()
    }
}
