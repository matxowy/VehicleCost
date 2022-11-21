package com.matxowy.vehiclecost.data.db.dao

import androidx.room.*
import com.matxowy.vehiclecost.data.db.entity.Repair
import kotlinx.coroutines.flow.Flow

@Dao
interface RepairDao {

    @Query("SELECT max(cost) FROM repair")
    fun getMaxCostOfRepair() : Flow<Double>

    @Query("SELECT sum(cost) FROM repair")
    fun getSumCostOfRepair() : Flow<Double>

    @Query("SELECT cost FROM repair ORDER BY date DESC, mileage DESC LIMIT 1")
    fun getLastCost() : Flow<Double>

    @Query("SELECT * FROM repair ORDER BY mileage DESC, date DESC")
    fun getRepairs() : Flow<List<Repair>>

    @Query("SELECT mileage FROM repair ORDER BY mileage DESC LIMIT 1")
    fun getLastMileage() : Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(repair: Repair)

    @Update
    suspend fun update(repair: Repair)

    @Delete
    suspend fun delete(repair: Repair)

}