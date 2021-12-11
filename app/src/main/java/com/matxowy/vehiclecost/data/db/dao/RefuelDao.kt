package com.matxowy.vehiclecost.data.db.dao

import androidx.room.*
import com.matxowy.vehiclecost.data.db.entity.Refuel
import kotlinx.coroutines.flow.Flow

@Dao
interface RefuelDao {

    @Query("SELECT * FROM refuel ORDER BY date DESC")
    fun getRefuels() : Flow<List<Refuel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(refuel: Refuel)

    @Update
    suspend fun update(refuel: Refuel)

    @Delete
    suspend fun delete(refuel: Refuel)
}