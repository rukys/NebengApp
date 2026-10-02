package com.disinidev.nebeng.di

import com.disinidev.nebeng.data.repository.AuthRepositoryImpl
import com.disinidev.nebeng.data.repository.BookingRepositoryImpl
import com.disinidev.nebeng.data.repository.ChatRepositoryImpl
import com.disinidev.nebeng.data.repository.NotificationRepositoryImpl
import com.disinidev.nebeng.data.repository.RoutineRepositoryImpl
import com.disinidev.nebeng.data.repository.TripLocationRepositoryImpl
import com.disinidev.nebeng.data.repository.UserRepositoryImpl
import com.disinidev.nebeng.data.repository.VehicleRepositoryImpl
import com.disinidev.nebeng.domain.repository.AuthRepository
import com.disinidev.nebeng.domain.repository.BookingRepository
import com.disinidev.nebeng.domain.repository.ChatRepository
import com.disinidev.nebeng.domain.repository.NotificationRepository
import com.disinidev.nebeng.domain.repository.RoutineRepository
import com.disinidev.nebeng.domain.repository.TripLocationRepository
import com.disinidev.nebeng.domain.repository.UserRepository
import com.disinidev.nebeng.domain.repository.VehicleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BookingModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

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

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        impl: ChatRepositoryImpl
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindVehicleRepository(
        impl: VehicleRepositoryImpl
    ): VehicleRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        impl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindRoutineRepository(
        impl: RoutineRepositoryImpl
    ): RoutineRepository
}
