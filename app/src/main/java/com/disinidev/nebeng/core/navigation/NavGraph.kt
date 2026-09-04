package com.disinidev.nebeng.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.disinidev.nebeng.core.designsystem.NebengColor

@Composable
fun NebengNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: NavDestination = NavDestination.Splash
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // --- Auth Flow ---
        composable<NavDestination.Splash> {
            com.disinidev.nebeng.presentation.auth.splash.SplashScreen(
                onNavigateToHome = {
                    navController.navigate(NavDestination.Home) {
                        popUpTo(NavDestination.Splash) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(NavDestination.Onboarding) {
                        popUpTo(NavDestination.Splash) { inclusive = true }
                    }
                }
            )
        }

        composable<NavDestination.Onboarding> {
            com.disinidev.nebeng.presentation.auth.onboarding.OnboardingScreen(
                onNavigateToRegister = {
                    navController.navigate(NavDestination.Register) {
                        popUpTo(NavDestination.Onboarding) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(NavDestination.Login) {
                        popUpTo(NavDestination.Onboarding) { inclusive = true }
                    }
                }
            )
        }

        composable<NavDestination.Login> {
            com.disinidev.nebeng.presentation.auth.login.LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(NavDestination.Register) {
                        popUpTo(NavDestination.Login) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToHome = {
                    navController.navigate(NavDestination.Home) {
                        popUpTo(NavDestination.Login) { inclusive = true }
                    }
                }
            )
        }

        composable<NavDestination.Register> {
            com.disinidev.nebeng.presentation.auth.register.RegisterScreen(
                onNavigateToOtp = { phone, name, email ->
                    navController.navigate(
                        NavDestination.Otp(
                            phoneNumber = phone,
                            fullName = name,
                            email = email
                        )
                    )
                },
                onNavigateToLogin = {
                    navController.navigate(NavDestination.Login) {
                        popUpTo(NavDestination.Register) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<NavDestination.Otp> { backStackEntry ->
            val route = backStackEntry.toRoute<NavDestination.Otp>()
            com.disinidev.nebeng.presentation.auth.otp.OtpScreen(
                phoneNumber = route.phoneNumber,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToNext = {
                    navController.navigate(
                        NavDestination.SetupProfile(
                            phoneNumber = route.phoneNumber,
                            fullName = route.fullName,
                            email = route.email
                        )
                    ) {
                        popUpTo(NavDestination.Register) { inclusive = true }
                    }
                }
            )
        }

        composable<NavDestination.SetupProfile> { backStackEntry ->
            val route = backStackEntry.toRoute<NavDestination.SetupProfile>()
            com.disinidev.nebeng.presentation.auth.setup.SetupProfileScreen(
                phoneNumber = route.phoneNumber,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToNext = {
                    navController.navigate(NavDestination.Home) {
                        popUpTo(NavDestination.Splash) { inclusive = true }
                    }
                }
            )
        }

        // --- Main Tabs ---
        composable<NavDestination.Home> {
            com.disinidev.nebeng.presentation.home.HomeScreen(
                onNavigateToNotifications = {
                    navController.navigate(NavDestination.Notifications)
                },
                onNavigateToSearch = { vehicleType ->
                    navController.navigate(NavDestination.Search(vehicleType = vehicleType))
                },
                onNavigateToOfferRide = {
                    navController.navigate(NavDestination.Search(vehicleType = "car"))
                },
                onNavigateToRoutine = {
                    navController.navigate(NavDestination.Search(vehicleType = "car"))
                },
                onNavigateToRideDetail = { rideId ->
                    navController.navigate(NavDestination.RideDetail(rideId = rideId))
                },
                onNavigateToCheckout = { rideId ->
                    navController.navigate(NavDestination.Checkout(rideId = rideId, seatPosition = "A2"))
                },
                onTabSelected = { tab ->
                    when (tab) {
                        com.disinidev.nebeng.core.component.NebengTab.BERANDA -> { /* already on home */ }
                        com.disinidev.nebeng.core.component.NebengTab.AKTIVITAS -> navController.navigate(NavDestination.Activity)
                        com.disinidev.nebeng.core.component.NebengTab.PESAN -> navController.navigate(NavDestination.Messages)
                        com.disinidev.nebeng.core.component.NebengTab.AKUN -> navController.navigate(NavDestination.Profile)
                    }
                }
            )
        }

        composable<NavDestination.Activity> {
            PlaceholderScreen(name = "Activity Screen")
        }

        composable<NavDestination.Messages> {
            PlaceholderScreen(name = "Messages Screen")
        }

        composable<NavDestination.Profile> {
            PlaceholderScreen(name = "Profile Screen")
        }

        composable<NavDestination.Notifications> {
            com.disinidev.nebeng.presentation.notification.NotificationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLiveTracking = { bookingId ->
                    navController.navigate(NavDestination.LiveTracking(bookingId = bookingId))
                }
            )
        }

        // --- Search & Rides ---
        composable<NavDestination.Search> { backStackEntry ->
            val route = backStackEntry.toRoute<NavDestination.Search>()
            PlaceholderScreen(name = "Search (${route.vehicleType})")
        }

        composable<NavDestination.SearchResults> { backStackEntry ->
            val route = backStackEntry.toRoute<NavDestination.SearchResults>()
            PlaceholderScreen(name = "Search Results: ${route.pickupAddress} -> ${route.dropoffAddress}")
        }

        composable<NavDestination.RideDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<NavDestination.RideDetail>()
            PlaceholderScreen(name = "Ride Detail: ${route.rideId}")
        }

        // --- Booking & Payment ---
        composable<NavDestination.Checkout> { backStackEntry ->
            val route = backStackEntry.toRoute<NavDestination.Checkout>()
            PlaceholderScreen(name = "Checkout: Ride ${route.rideId}, Seat ${route.seatPosition}")
        }

        composable<NavDestination.Payment> { backStackEntry ->
            val route = backStackEntry.toRoute<NavDestination.Payment>()
            PlaceholderScreen(name = "Payment for Booking ${route.bookingId} - Rp ${route.amount}")
        }

        composable<NavDestination.QrisPayment> { backStackEntry ->
            val route = backStackEntry.toRoute<NavDestination.QrisPayment>()
            PlaceholderScreen(name = "QRIS Payment ${route.paymentId}")
        }

        // --- Tracking ---
        composable<NavDestination.LiveTracking> { backStackEntry ->
            val route = backStackEntry.toRoute<NavDestination.LiveTracking>()
            PlaceholderScreen(name = "Live Tracking for Booking ${route.bookingId}")
        }

        composable<NavDestination.TripDone> { backStackEntry ->
            val route = backStackEntry.toRoute<NavDestination.TripDone>()
            PlaceholderScreen(name = "Trip Done for Booking ${route.bookingId}")
        }

        composable<NavDestination.Tip> { backStackEntry ->
            val route = backStackEntry.toRoute<NavDestination.Tip>()
            PlaceholderScreen(name = "Tip QRIS for Booking ${route.bookingId}")
        }
    }
}

@Composable
private fun PlaceholderScreen(
    name: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NebengColor.Primary0),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            color = NebengColor.Gray800
        )
    }
}
