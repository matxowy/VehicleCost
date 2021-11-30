package com.matxowy.vehiclecost.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.matxowy.vehiclecost.data.db.dao.RefuelDao
import com.matxowy.vehiclecost.data.db.dao.RepairDao
import com.matxowy.vehiclecost.data.db.entity.Refuel
import com.matxowy.vehiclecost.data.db.entity.Repair
import com.matxowy.vehiclecost.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(
    entities = [Refuel::class, Repair::class],
    version = 1
)
abstract class VehicleCostDatabase : RoomDatabase() {

    abstract fun refuelDao(): RefuelDao
    abstract fun repairDao(): RepairDao

    class Callback @Inject constructor(
        private val database: Provider<VehicleCostDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val refuelDao = database.get().refuelDao()
            val repairDao = database.get().repairDao()

            applicationScope.launch {
                refuelDao.insert(Refuel(174889,
                "2021-11-03",
                "17:34",
                37.84,
                174.88,
                6.04,
                true))

                refuelDao.insert(Refuel(224879,
                "2021-11-25",
                "10:21",
                12.44,
                88.33,
                6.04))

                repairDao.insert(Repair("Wymiana rozrządu",
                    178993,
                550.0,
                "2021-06-03",
                "12:00",
                ))
            }
        }
    }
}
