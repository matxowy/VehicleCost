package com.matxowy.vehiclecost.data.db.dao

import androidx.room.*
import com.matxowy.vehiclecost.data.db.entity.Refuel
import kotlinx.coroutines.flow.Flow

@Dao
interface RefuelDao {

    @Query("SELECT sum(amountOfFuel) FROM refuel WHERE vehicleId = :vehicleId")
    fun getSumOfRefuels(vehicleId: Int) : Double

    @Query("SELECT sum(cost) FROM refuel WHERE vehicleId = :vehicleId")
    fun getSumOfCosts(vehicleId: Int) : Double

    @Query("SELECT price FROM refuel WHERE vehicleId = :vehicleId ORDER BY mileage DESC, date DESC LIMIT 1")
    fun getLastPriceOfFuel(vehicleId: Int) : Double

    @Query("SELECT mileage FROM refuel WHERE vehicleId = :vehicleId ORDER BY mileage DESC LIMIT 1")
    fun getLastMileage(vehicleId: Int) : Flow<Int>

    @Query("SELECT * FROM refuel WHERE vehicleId = :vehicleId ORDER BY mileage DESC, date DESC")
    fun getRefuels(vehicleId: Int) : Flow<List<Refuel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(refuel: Refuel)

    @Update
    suspend fun update(refuel: Refuel)

    @Delete
    suspend fun delete(refuel: Refuel)
}