package com.disinidev.nebeng.core.navigation

import kotlinx.serialization.Serializable

sealed interface NavDestination {
    // Auth Flow
    @Serializable
    data object Splash : NavDestination

    @Serializable
    data object Onboarding : NavDestination

    @Serializable
    data object Login : NavDestination

    @Serializable
    data object Register : NavDestination

    @Serializable
    data class Otp(
        val phoneNumber: String,
        val fullName: String = "",
        val email: String = ""
    ) : NavDestination

    @Serializable
    data class SetupProfile(
        val phoneNumber: String,
        val fullName: String = "",
        val email: String = ""
    ) : NavDestination

    // Main Tabs Flow
    @Serializable
    data object Home : NavDestination

    @Serializable
    data object Activity : NavDestination

    @Serializable
    data object Messages : NavDestination

    @Serializable
    data class ChatDetail(
        val driverName: String = "Andi Pratama",
        val vehicleInfo: String = "Avanza Silver",
        val pin: String = "489 201"
    ) : NavDestination

    @Serializable
    data object Profile : NavDestination

    @Serializable
    data object Settings : NavDestination

    @Serializable
    data object EditProfile : NavDestination

    @Serializable
    data object Notifications : NavDestination

    // Search & Booking Flow
    @Serializable
    data class Search(val vehicleType: String = "car") : NavDestination

    @Serializable
    data class SearchResults(
        val pickupAddress: String,
        val dropoffAddress: String,
        val vehicleType: String,
        val pickupLat: Double = -6.2297,
        val pickupLng: Double = 106.8580,
        val departureTime: String = "Hari Ini, 07:30"
    ) : NavDestination

    @Serializable
    data class RideDetail(val rideId: String) : NavDestination

    @Serializable
    data class CheckoutCar(val rideId: String) : NavDestination

    @Serializable
    data class CheckoutMotor(val rideId: String) : NavDestination

    @Serializable
    data class Payment(val bookingId: String, val amount: Int) : NavDestination

    @Serializable
    data class QrisPayment(val bookingId: String, val paymentId: String) : NavDestination

    // Tracking & Tip Flow
    @Serializable
    data class LiveTracking(val bookingId: String) : NavDestination

    @Serializable
    data class TripDone(val bookingId: String) : NavDestination

    @Serializable
    data class Tip(val bookingId: String) : NavDestination
}
