package com.matxowy.vehiclecost.data.localpreferences.di

import com.matxowy.vehiclecost.data.localpreferences.LocalPreferencesApi
import com.matxowy.vehiclecost.data.localpreferences.LocalPreferencesService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalPreferencesModule {

    @Singleton
    @Binds
    abstract fun provideLocalPreferences(localPreferencesService: LocalPreferencesService): LocalPreferencesApi
}