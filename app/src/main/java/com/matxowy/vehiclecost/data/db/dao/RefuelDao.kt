package com.matxowy.vehiclecost.data.db.dao

import androidx.room.*
import com.matxowy.vehiclecost.data.db.entity.Refuel
import kotlinx.coroutines.flow.Flow

@Dao
interface RefuelDao {

    @Query("SELECT sum(amountOfFuel) FROM refuel")
    fun getSumOfRefuels() : Flow<Double>

    @Query("SELECT sum(cost) FROM refuel")
    fun getSumOfCosts() : Flow<Double>

    @Query("SELECT price FROM refuel ORDER BY mileage DESC, date DESC LIMIT 1")
    fun getLastPriceOfFuel() : Flow<Double>

    @Query("SELECT mileage FROM refuel ORDER BY mileage DESC LIMIT 1")
    fun getLastMileage() : Flow<Int>

    @Query("SELECT * FROM refuel ORDER BY mileage DESC, date DESC")
    fun getRefuels() : Flow<List<Refuel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(refuel: Refuel)

    @Update
    suspend fun update(refuel: Refuel)

    @Delete
    suspend fun delete(refuel: Refuel)
}