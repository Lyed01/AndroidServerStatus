package com.lyed.minecraftserverstatus.data.dbLocal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ServerDAO {
    @Query("SELECT * FROM servers")
    fun getAll(): List<ServerLocal>

    @Query("SELECT * FROM servers WHERE ip = :ip AND hostname = :hostname LIMIT 1")
    fun getByPk(ip: String, hostname: String): ServerLocal

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg server: ServerLocal)

    @Delete
    fun delete(server: ServerLocal)



}
