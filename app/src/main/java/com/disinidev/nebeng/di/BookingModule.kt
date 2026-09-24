package com.disinidev.nebeng.di

import com.disinidev.nebeng.data.repository.BookingRepositoryImpl
import com.disinidev.nebeng.domain.repository.BookingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import com.disinidev.nebeng.data.repository.TripLocationRepositoryImpl
import com.disinidev.nebeng.domain.repository.TripLocationRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class BookingModule {

    @Binds
    @Singleton
    abstract fun bindBookingRepository(
        impl: BookingRepositoryImpl
    ): BookingRepository

    @Binds
    @Singleton
    abstract fun bindTripLocationRepository(
        impl: TripLocationRepositoryImpl
    ): TripLocationRepository
}
