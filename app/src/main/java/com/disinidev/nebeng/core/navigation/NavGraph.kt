package com.disinidev.nebeng.core.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(durationMillis = 300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth / 4 },
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(durationMillis = 200))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth / 4 },
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(durationMillis = 200))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(durationMillis = 300))
        }
    ) {
        // --- Auth Flow ---
        composable<NavDestination.Splash>(
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            com.disinidev.nebeng.presentation.auth.splash.SplashScreen(
                onNavigateToHome = {
                    navController.navigate(NavDestination.Home) {
                        popUpTo(NavDestination.Splash) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(NavDestination.Login) {
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
                        popUpTo(navController.graph.id) { inclusive = true }
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
                },
                onNavigateToHome = {
                    navController.navigate(NavDestination.Home) {
                        popUpTo(navController.graph.id) { inclusive = true }
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
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }

        // --- Main Tabs ---
        composable<NavDestination.Home>(
            enterTransition = { fadeIn(animationSpec = tween(150)) },
            exitTransition = { fadeOut(animationSpec = tween(150)) },
            popEnterTransition = { fadeIn(animationSpec = tween(150)) },
            popExitTransition = { fadeOut(animationSpec = tween(150)) }
        ) {
            com.disinidev.nebeng.presentation.home.HomeScreen(
                onNavigateToNotifications = {
                    navController.navigate(NavDestination.Notifications)
                },
                onNavigateToSearch = { vehicleType ->
                    navController.navigate(NavDestination.Search(vehicleType = vehicleType))
                },
                onNavigateToOfferRide = {
                    navController.navigate(NavDestination.OfferRide)
                },
                onNavigateToRoutine = {
                    navController.navigate(NavDestination.Search(vehicleType = "car"))
                },
                onNavigateToRideDetail = { rideId ->
                    navController.navigate(NavDestination.RideDetail(rideId = rideId))
                },
                onNavigateToCheckout = { rideId ->
                    navController.navigate(NavDestination.CheckoutCar(rideId = rideId))
                },
                onNavigateToCheckoutCar = { rideId ->
                    navController.navigate(NavDestination.CheckoutCar(rideId = rideId))
                },
                onNavigateToCheckoutMotor = { rideId ->
                    navController.navigate(NavDestination.CheckoutMotor(rideId = rideId))
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

        composable<NavDestination.Activity>(
            enterTransition = { fadeIn(animationSpec = tween(150)) },
            exitTransition = { fadeOut(animationSpec = tween(150)) },
            popEnterTransition = { fadeIn(animationSpec = tween(150)) },
            popExitTransition = { fadeOut(animationSpec = tween(150)) }
        ) {
            com.disinidev.nebeng.presentation.activity.ActivityScreen(
                onTabSelected = { tab ->
                    when (tab) {
                        com.disinidev.nebeng.core.component.NebengTab.BERANDA -> navController.navigate(NavDestination.Home)
                        com.disinidev.nebeng.core.component.NebengTab.AKTIVITAS -> { /* already on activity */ }
                        com.disinidev.nebeng.core.component.NebengTab.PESAN -> navController.navigate(NavDestination.Messages)
                        com.disinidev.nebeng.core.component.NebengTab.AKUN -> navController.navigate(NavDestination.Profile)
                    }
                },
                onNavigateToLiveTracking = { bookingId ->
                    navController.navigate(NavDestination.LiveTracking(bookingId = bookingId))
                },
                onNavigateToTripDone = { bookingId ->
                    navController.navigate(NavDestination.TripDone(bookingId = bookingId))
                }
            )
        }

        composable<NavDestination.Messages>(
            enterTransition = { fadeIn(animationSpec = tween(150)) },
            exitTransition = { fadeOut(animationSpec = tween(150)) },
            popEnterTransition = { fadeIn(animationSpec = tween(150)) },
            popExitTransition = { fadeOut(animationSpec = tween(150)) }
        ) {
            com.disinidev.nebeng.presentation.chat.ConversationsScreen(
                onTabSelected = { tab ->
                    when (tab) {
                        com.disinidev.nebeng.core.component.NebengTab.BERANDA -> navController.navigate(NavDestination.Home)
                        com.disinidev.nebeng.core.component.NebengTab.AKTIVITAS -> navController.navigate(NavDestination.Activity)
                        com.disinidev.nebeng.core.component.NebengTab.PESAN -> { /* already on messages */ }
                        com.disinidev.nebeng.core.component.NebengTab.AKUN -> navController.navigate(NavDestination.Profile)
                    }
                },
                onNavigateToChat = { driverName, vehicleInfo, pin ->
                    navController.navigate(
                        NavDestination.ChatDetail(
                            driverName = driverName,
                            vehicleInfo = vehicleInfo,
                            pin = pin
                        )
                    )
                }
            )
        }

        composable<NavDestination.ChatDetail> { backStackEntry ->
            com.disinidev.nebeng.presentation.chat.ChatScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<NavDestination.Profile>(
            enterTransition = { fadeIn(animationSpec = tween(150)) },
            exitTransition = { fadeOut(animationSpec = tween(150)) },
            popEnterTransition = { fadeIn(animationSpec = tween(150)) },
            popExitTransition = { fadeOut(animationSpec = tween(150)) }
        ) {
            com.disinidev.nebeng.presentation.profile.ProfileScreen(
                onNavigateToSettings = {
                    navController.navigate(NavDestination.Settings)
                },
                onNavigateToEditProfile = {
                    navController.navigate(NavDestination.EditProfile)
                },
                onTabSelected = { tab ->
                    when (tab) {
                        com.disinidev.nebeng.core.component.NebengTab.BERANDA -> navController.navigate(NavDestination.Home)
                        com.disinidev.nebeng.core.component.NebengTab.AKTIVITAS -> navController.navigate(NavDestination.Activity)
                        com.disinidev.nebeng.core.component.NebengTab.PESAN -> navController.navigate(NavDestination.Messages)
                        com.disinidev.nebeng.core.component.NebengTab.AKUN -> { /* already on profile */ }
                    }
                }
            )
        }

        composable<NavDestination.Settings> {
            com.disinidev.nebeng.presentation.settings.SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEditProfile = {
                    navController.navigate(NavDestination.EditProfile)
                },
                onNavigateToChangePassword = {
                    navController.navigate(NavDestination.ChangePassword)
                },
                onLogoutSuccess = {
                    navController.navigate(NavDestination.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<NavDestination.ChangePassword> {
            com.disinidev.nebeng.presentation.settings.password.ChangePasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<NavDestination.EditProfile> {
            com.disinidev.nebeng.presentation.profile.edit.EditProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
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
            com.disinidev.nebeng.presentation.search.form.SearchFormScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSearchResults = { pickup, dropoff, vehicleType, pickupLat, pickupLng, departureTime ->
                    navController.navigate(
                        NavDestination.SearchResults(
                            pickupAddress = pickup,
                            dropoffAddress = dropoff,
                            vehicleType = vehicleType,
                            pickupLat = pickupLat,
                            pickupLng = pickupLng,
                            departureTime = departureTime
                        )
                    )
                }
            )
        }

        composable<NavDestination.SearchResults> { backStackEntry ->
            val route = backStackEntry.toRoute<NavDestination.SearchResults>()
            com.disinidev.nebeng.presentation.search.results.SearchResultsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEditRouteClick = {
                    navController.popBackStack()
                },
                onNavigateToRideDetail = { rideId ->
                    navController.navigate(NavDestination.RideDetail(rideId = rideId))
                },
                onNavigateToCheckoutCar = { rideId ->
                    navController.navigate(NavDestination.CheckoutCar(rideId = rideId))
                },
                onNavigateToCheckoutMotor = { rideId ->
                    navController.navigate(NavDestination.CheckoutMotor(rideId = rideId))
                },
                onNavigateToOfferRide = {
                    navController.navigate(NavDestination.OfferRide)
                }
            )
        }

        composable<NavDestination.OfferRide> {
            com.disinidev.nebeng.presentation.driver.offer.OfferRideScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPublishSuccess = {
                    navController.navigate(NavDestination.Home) {
                        popUpTo(NavDestination.Home) { inclusive = true }
                    }
                }
            )
        }

        composable<NavDestination.RideDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<NavDestination.RideDetail>()
            val isMotor = route.rideId.contains("motor", ignoreCase = true) || route.rideId.contains("ride_2", ignoreCase = true)
            if (isMotor) {
                com.disinidev.nebeng.presentation.checkout.CheckoutMotorScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onConfirmBooking = { bookingId, _ ->
                        navController.navigate(NavDestination.LiveTracking(bookingId = bookingId))
                    }
                )
            } else {
                com.disinidev.nebeng.presentation.checkout.CheckoutCarScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onConfirmBooking = { bookingId, _ ->
                        navController.navigate(NavDestination.LiveTracking(bookingId = bookingId))
                    }
                )
            }
        }

        // --- Booking Flow ---
        composable<NavDestination.CheckoutCar> { backStackEntry ->
            val route = backStackEntry.toRoute<NavDestination.CheckoutCar>()
            com.disinidev.nebeng.presentation.checkout.CheckoutCarScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onConfirmBooking = { bookingId, seatPosition ->
                    navController.navigate(NavDestination.LiveTracking(bookingId = bookingId))
                }
            )
        }

        composable<NavDestination.CheckoutMotor> { backStackEntry ->
            val route = backStackEntry.toRoute<NavDestination.CheckoutMotor>()
            com.disinidev.nebeng.presentation.checkout.CheckoutMotorScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onConfirmBooking = { bookingId, helmetChoice ->
                    navController.navigate(NavDestination.LiveTracking(bookingId = bookingId))
                }
            )
        }

        composable<NavDestination.Payment> { backStackEntry ->
            val route = backStackEntry.toRoute<NavDestination.Payment>()
            androidx.compose.runtime.LaunchedEffect(Unit) {
                navController.navigate(NavDestination.LiveTracking(bookingId = route.bookingId)) {
                    popUpTo(NavDestination.Payment(bookingId = route.bookingId, amount = route.amount)) { inclusive = true }
                }
            }
        }

        composable<NavDestination.QrisPayment> { backStackEntry ->
            val route = backStackEntry.toRoute<NavDestination.QrisPayment>()
            androidx.compose.runtime.LaunchedEffect(Unit) {
                navController.navigate(NavDestination.LiveTracking(bookingId = route.bookingId)) {
                    popUpTo(NavDestination.QrisPayment(bookingId = route.bookingId, paymentId = route.paymentId)) { inclusive = true }
                }
            }
        }

        // --- Tracking ---
        composable<NavDestination.LiveTracking> { backStackEntry ->
            val route = backStackEntry.toRoute<NavDestination.LiveTracking>()
            com.disinidev.nebeng.presentation.tracking.LiveTrackingScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToChat = { driverName, bookingId ->
                    navController.navigate(
                        NavDestination.ChatDetail(
                            driverName = driverName,
                            vehicleInfo = if (bookingId.contains("ride_2")) "Yamaha NMAX Hitam" else "Avanza Silver",
                            pin = if (bookingId.contains("ride_2")) "215 889" else "489 201"
                        )
                    )
                },
                onTripFinished = { bookingId ->
                    navController.navigate(NavDestination.TripDone(bookingId = bookingId)) {
                        popUpTo(NavDestination.LiveTracking(bookingId = bookingId)) { inclusive = true }
                    }
                }
            )
        }

        composable<NavDestination.TripDone> { backStackEntry ->
            val route = backStackEntry.toRoute<NavDestination.TripDone>()
            com.disinidev.nebeng.presentation.tripdone.TripDoneScreen(
                onNavigateBack = { navController.popBackStack() },
                onSkip = {
                    navController.navigate(NavDestination.Home) {
                        popUpTo(NavDestination.Home) { inclusive = true }
                    }
                },
                onSubmitComplete = {
                    navController.navigate(NavDestination.Home) {
                        popUpTo(NavDestination.Home) { inclusive = true }
                    }
                }
            )
        }

        composable<NavDestination.Tip> { backStackEntry ->
            val route = backStackEntry.toRoute<NavDestination.Tip>()
            androidx.compose.runtime.LaunchedEffect(Unit) {
                navController.navigate(NavDestination.Home) {
                    popUpTo(NavDestination.Home) { inclusive = true }
                }
            }
        }
    }
}
