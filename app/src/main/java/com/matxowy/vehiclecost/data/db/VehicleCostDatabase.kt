package com.matxowy.vehiclecost.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.matxowy.vehiclecost.data.db.entity.Refuel
import com.matxowy.vehiclecost.data.db.entity.Repair

@Database(
    entities = [Refuel::class, Repair::class],
    version = 1
)
abstract class VehicleCostDatabase : RoomDatabase() {
}