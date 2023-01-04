package com.matxowy.vehiclecost.data.db.dao

import androidx.room.*
import com.matxowy.vehiclecost.data.db.entity.Vehicle
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {

    @Query("SELECT * FROM vehicle")
    fun getVehicles(): Flow<List<Vehicle>>

    @Query("SELECT name FROM vehicle")
    fun getVehiclesNames(): Flow<List<String>>

    @Query("SELECT * FROM vehicle WHERE vehicleId = :id")
    fun getVehicleById(id: Int): Flow<Vehicle>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vehicle: Vehicle)

    @Update
    suspend fun update(vehicle: Vehicle)

    @Delete
    suspend fun delete(vehicle: Vehicle)
}