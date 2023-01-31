package com.matxowy.vehiclecost.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.matxowy.vehiclecost.R
import com.matxowy.vehiclecost.data.db.dao.RefuelDao
import com.matxowy.vehiclecost.data.db.dao.RepairDao
import com.matxowy.vehiclecost.data.db.dao.VehicleDao
import com.matxowy.vehiclecost.data.db.entity.Refuel
import com.matxowy.vehiclecost.data.db.entity.Repair
import com.matxowy.vehiclecost.data.db.entity.Vehicle
import com.matxowy.vehiclecost.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(
    entities = [Refuel::class, Repair::class, Vehicle::class],
    version = 1
)
abstract class VehicleCostDatabase : RoomDatabase() {

    abstract fun refuelDao(): RefuelDao
    abstract fun repairDao(): RepairDao
    abstract fun vehicleDao(): VehicleDao

    class Callback @Inject constructor(
        private val database: Provider<VehicleCostDatabase>,
        @ApplicationContext private val context: Context,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val refuelDao = database.get().refuelDao()
            val repairDao = database.get().repairDao()
            val vehicleDao = database.get().vehicleDao()

            applicationScope.launch {
                vehicleDao.insert(
                    Vehicle(
                        name = context.getString(R.string.default_vehicle),
                    )

                )
                vehicleDao.insert(
                    Vehicle(
                        name = "My Car2",
                    )

                )

                refuelDao.insert(
                    Refuel(
                        mileage = 174889,
                        date = "2021-11-03",
                        time = "17:34",
                        amountOfFuel = 37.84,
                        cost = 174.88,
                        price = 6.04,
                        fuelType = "ON",
                        fullRefueled = true,
                        vehicleId = 2,
                    )
                )

                refuelDao.insert(
                    Refuel(
                        mileage = 224879,
                        date = "2021-11-25",
                        time = "10:21",
                        amountOfFuel = 12.44,
                        cost = 88.33,
                        price = 6.04,
                        fuelType = "ON",
                        vehicleId = 1,
                    )
                )

                refuelDao.insert(
                    Refuel(
                        mileage = 256222,
                        date = "2021-12-15",
                        time = "15:22",
                        amountOfFuel = 12.0,
                        cost = 56.0,
                        price = 6.0,
                        fuelType = "ON",
                        fullRefueled = false,
                        comments = "Tankowanie przy rezerwie",
                        vehicleId = 1,
                    )
                )

                repairDao.insert(
                    Repair(
                        title = "Wymiana rozrządu",
                        mileage = 178993,
                        cost = 550.0,
                        date = "2021-06-03",
                        time = "12:00",
                        vehicleId = 2,
                    )
                )
            }
        }
    }
}
