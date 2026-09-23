package com.disinidev.nebeng.di

import com.disinidev.nebeng.data.repository.RideRepositoryImpl
import com.disinidev.nebeng.domain.repository.RideRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RideModule {

    @Binds
    @Singleton
    abstract fun bindRideRepository(
        impl: RideRepositoryImpl
    ): RideRepository
}
