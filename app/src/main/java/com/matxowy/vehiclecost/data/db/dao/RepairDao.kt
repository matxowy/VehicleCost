package com.matxowy.vehiclecost.data.db.dao

import androidx.room.*
import com.matxowy.vehiclecost.data.db.entity.Repair
import kotlinx.coroutines.flow.Flow

@Dao
interface RepairDao {

    @Query("SELECT max(cost) FROM repair WHERE vehicleId = :vehicleId")
    fun getMaxCostOfRepair(vehicleId: Int) : Double

    @Query("SELECT sum(cost) FROM repair WHERE vehicleId = :vehicleId")
    fun getSumCostOfRepair(vehicleId: Int) : Double

    @Query("SELECT cost FROM repair WHERE vehicleId = :vehicleId ORDER BY date DESC, mileage DESC LIMIT 1")
    fun getLastCost(vehicleId: Int) : Double

    @Query("SELECT * FROM repair WHERE vehicleId = :vehicleId ORDER BY mileage DESC, date DESC")
    fun getRepairs(vehicleId: Int) : Flow<List<Repair>>

    @Query("SELECT mileage FROM repair WHERE vehicleId = :vehicleId ORDER BY mileage DESC LIMIT 1")
    fun getLastMileage(vehicleId: Int) : Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(repair: Repair)

    @Update
    suspend fun update(repair: Repair)

    @Delete
    suspend fun delete(repair: Repair)

}