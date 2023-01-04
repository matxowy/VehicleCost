package com.matxowy.vehiclecost.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.matxowy.vehiclecost.data.db.VehicleCostDatabase
import com.matxowy.vehiclecost.data.localpreferences.di.LocalPreferencesModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/*
Dependency Injection
*/

@Module(includes = [LocalPreferencesModule::class])
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
       app: Application,
       callback: VehicleCostDatabase.Callback
    ) = Room.databaseBuilder(app, VehicleCostDatabase::class.java, "vehicle_cost_database")
        .fallbackToDestructiveMigration()
        .addCallback(callback)
        .build()

    @Provides
    fun provideRefuelDao(db: VehicleCostDatabase) = db.refuelDao()

    @Provides
    fun provideRepairDao(db: VehicleCostDatabase) = db.repairDao()

    @Provides
    fun provideVehicleDao(db: VehicleCostDatabase) = db.vehicleDao()

    @Singleton
    @Provides
    fun provideSharedPreference(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences("vehicle_cost_preferences", Context.MODE_PRIVATE)

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())
}

// Klasa do rozróżniania Scope przez Daggera po dodaniu innego niż ApplicationScope
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope