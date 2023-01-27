package com.matxowy.vehiclecost.data.db.dao

import androidx.room.*
import com.matxowy.vehiclecost.data.db.entity.Vehicle
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {

    @Query("SELECT * FROM vehicle")
    fun getVehicles(): Flow<List<Vehicle>>

    @Query("SELECT name FROM vehicle ORDER BY vehicleId")
    fun getVehiclesNames(): Flow<List<String>>

    @Query("SELECT * FROM vehicle WHERE vehicleId = :id")
    fun getVehicleById(id: Int): Vehicle

    @Query("SELECT * FROM vehicle WHERE name = :name")
    fun getVehicleByName(name: String): Vehicle

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(vehicle: Vehicle)

    @Update
    suspend fun update(vehicle: Vehicle)

    @Delete
    suspend fun delete(vehicle: Vehicle)
}