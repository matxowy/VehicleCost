package com.matxowy.vehiclecost.di

import android.app.Application
import androidx.room.Room
import com.matxowy.vehiclecost.data.db.VehicleCostDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/*
Dependency Injection
*/

@Module
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

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())
}

// Klasa do rozróżniania Scope przez Daggera po dodaniu innego niż ApplicationScope
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope