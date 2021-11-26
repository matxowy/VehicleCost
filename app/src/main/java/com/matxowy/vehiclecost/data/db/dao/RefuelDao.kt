package com.matxowy.vehiclecost.data.db.dao

import androidx.room.*
import com.matxowy.vehiclecost.data.db.entity.Refuel

@Dao
interface RefuelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(refuel: Refuel)

    @Update
    suspend fun update(refuel: Refuel)

    @Delete
    suspend fun delete(refuel: Refuel)
}