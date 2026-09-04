package com.disinidev.nebeng.di

import android.content.Context
import com.disinidev.nebeng.core.location.DefaultLocationClient
import com.disinidev.nebeng.core.location.LocationClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {

    @Binds
    @Singleton
    abstract fun bindLocationClient(
        defaultLocationClient: DefaultLocationClient
    ): LocationClient

    companion object {
        @Provides
        @Singleton
        fun provideFusedLocationProviderClient(
            @ApplicationContext context: Context
        ): FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)
    }
}
