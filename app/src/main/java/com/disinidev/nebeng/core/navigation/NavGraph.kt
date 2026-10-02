package com.disinidev.nebeng.core.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.disinidev.nebeng.core.component.NebengTab
import com.disinidev.nebeng.presentation.activity.ActivityScreen
import com.disinidev.nebeng.presentation.auth.login.LoginScreen
import com.disinidev.nebeng.presentation.auth.onboarding.OnboardingScreen
import com.disinidev.nebeng.presentation.auth.otp.OtpScreen
import com.disinidev.nebeng.presentation.auth.register.RegisterScreen
import com.disinidev.nebeng.presentation.auth.setup.SetupProfileScreen
import com.disinidev.nebeng.presentation.auth.splash.SplashScreen
import com.disinidev.nebeng.presentation.chat.ChatScreen
import com.disinidev.nebeng.presentation.chat.ConversationsScreen
import com.disinidev.nebeng.presentation.checkout.CheckoutCarScreen
import com.disinidev.nebeng.presentation.checkout.CheckoutMotorScreen
import com.disinidev.nebeng.presentation.driver.offer.OfferRideScreen
import com.disinidev.nebeng.presentation.home.HomeScreen
import com.disinidev.nebeng.presentation.notification.NotificationScreen
import com.disinidev.nebeng.presentation.profile.ProfileScreen
import com.disinidev.nebeng.presentation.profile.edit.EditProfileScreen
import com.disinidev.nebeng.presentation.search.form.SearchFormScreen
import com.disinidev.nebeng.presentation.search.results.SearchResultsScreen
import com.disinidev.nebeng.presentation.settings.SettingsScreen
import com.disinidev.nebeng.presentation.settings.password.ChangePasswordScreen
import com.disinidev.nebeng.presentation.tracking.LiveTrackingScreen
import com.disinidev.nebeng.presentation.tip.TipScreen
import com.disinidev.nebeng.presentation.vehicle.VehicleManagementScreen
import com.disinidev.nebeng.presentation.tripdone.TripDoneScreen
import com.disinidev.nebeng.presentation.routine.RoutineCommuteScreen

@Composable
fun NebengNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: NavDestination = NavDestination.Splash,
    pendingDeepLinkUri: String? = null,
    onDeepLinkConsumed: () -> Unit = {}
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
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(NavDestination.Home) {
                        popUpTo(NavDestination.Splash) { inclusive = true }
                    }
                    if (!pendingDeepLinkUri.isNullOrBlank()) {
                        runCatching {
                            navController.navigate(android.net.Uri.parse(pendingDeepLinkUri))
                        }
                        onDeepLinkConsumed()
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

        composable<NavDestination.Onboarding>(
            deepLinks = listOf(navDeepLink { uriPattern = "nebeng://onboarding" })
        ) {
            OnboardingScreen(
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
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(NavDestination.Register) {
                        popUpTo(NavDestination.Login) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToHome = {
                    navController.navigate(NavDestination.Home) {
                        popUpTo(NavDestination.Login) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<NavDestination.Register> {
            RegisterScreen(
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
                        popUpTo(NavDestination.Register) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<NavDestination.Otp> { backStackEntry ->
            val route = backStackEntry.toRoute<NavDestination.Otp>()
            OtpScreen(
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
            SetupProfileScreen(
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
            HomeScreen(
                onNavigateToNotifications = {
                    navController.navigate(NavDestination.Notifications)
                },
                onNavigateToSearch = { vehicleType ->
                    navController.navigate(NavDestination.Search(vehicleType = vehicleType))
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
                },
                onNavigateToOfferRide = {
                    navController.navigate(NavDestination.OfferRide)
                },
                onNavigateToRoutine = {
                    navController.navigate(NavDestination.RoutineCommute)
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
                        NebengTab.BERANDA -> { /* already on home */ }
                        NebengTab.AKTIVITAS -> navController.navigate(NavDestination.Activity)
                        NebengTab.PESAN -> navController.navigate(NavDestination.Messages)
                        NebengTab.AKUN -> navController.navigate(NavDestination.Profile)
                    }
                }
            )
        }

        composable<NavDestination.Activity>(
            enterTransition = { fadeIn(animationSpec = tween(150)) },
            exitTransition = { fadeOut(animationSpec = tween(150)) },
            popEnterTransition = { fadeIn(animationSpec = tween(150)) },
            popExitTransition = { fadeOut(animationSpec = tween(150)) },
            deepLinks = listOf(navDeepLink { uriPattern = "nebeng://activity" })
        ) {
            ActivityScreen(
                onTabSelected = { tab ->
                    when (tab) {
                        NebengTab.BERANDA -> navController.navigate(NavDestination.Home)
                        NebengTab.AKTIVITAS -> { /* already on activity */ }
                        NebengTab.PESAN -> navController.navigate(NavDestination.Messages)
                        NebengTab.AKUN -> navController.navigate(NavDestination.Profile)
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
            popExitTransition = { fadeOut(animationSpec = tween(150)) },
            deepLinks = listOf(navDeepLink { uriPattern = "nebeng://messages" })
        ) {
            ConversationsScreen(
                onTabSelected = { tab ->
                    when (tab) {
                        NebengTab.BERANDA -> navController.navigate(NavDestination.Home)
                        NebengTab.AKTIVITAS -> navController.navigate(NavDestination.Activity)
                        NebengTab.PESAN -> { /* already on messages */ }
                        NebengTab.AKUN -> navController.navigate(NavDestination.Profile)
                    }
                },
                onNavigateToChat = { driverName, vehicleInfo, pin, bookingId, isTripCompleted ->
                    navController.navigate(
                        NavDestination.ChatDetail(
                            driverName = driverName,
                            vehicleInfo = vehicleInfo,
                            pin = pin,
                            bookingId = bookingId,
                            isTripCompleted = isTripCompleted
                        )
                    )
                }
            )
        }

        composable<NavDestination.ChatDetail>(
            deepLinks = listOf(
                navDeepLink { uriPattern = "nebeng://trip/{bookingId}/chat" },
                navDeepLink { uriPattern = "nebeng://chat/{bookingId}" }
            )
        ) { backStackEntry ->
            ChatScreen(
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
            ProfileScreen(
                onNavigateToSettings = {
                    navController.navigate(NavDestination.Settings)
                },
                onNavigateToEditProfile = {
                    navController.navigate(NavDestination.EditProfile)
                },
                onNavigateToVehicleManagement = {
                    navController.navigate(NavDestination.VehicleManagement)
                },
                onLogoutSuccess = {
                    navController.navigate(NavDestination.Login) {
                        popUpTo(navController.graph.id) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onTabSelected = { tab ->
                    when (tab) {
                        NebengTab.BERANDA -> navController.navigate(NavDestination.Home)
                        NebengTab.AKTIVITAS -> navController.navigate(NavDestination.Activity)
                        NebengTab.PESAN -> navController.navigate(NavDestination.Messages)
                        NebengTab.AKUN -> { /* already on profile */ }
                    }
                }
            )
        }

        composable<NavDestination.Settings> {
            SettingsScreen(
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
                        popUpTo(navController.graph.id) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<NavDestination.ChangePassword> {
            ChangePasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<NavDestination.EditProfile> {
            EditProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<NavDestination.Notifications>(
            deepLinks = listOf(navDeepLink { uriPattern = "nebeng://notifications" })
        ) {
            NotificationScreen(
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
            SearchFormScreen(
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
            SearchResultsScreen(
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
            OfferRideScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPublishSuccess = {
                    navController.navigate(NavDestination.Home) {
                        popUpTo(NavDestination.Home) { inclusive = true }
                    }
                },
                onNavigateToVehicleManagement = {
                    navController.navigate(NavDestination.VehicleManagement)
                }
            )
        }

        composable<NavDestination.RideDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<NavDestination.RideDetail>()
            val isMotor = route.rideId.contains("motor", ignoreCase = true) || route.rideId.contains("ride_2", ignoreCase = true)
            if (isMotor) {
                CheckoutMotorScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onConfirmBooking = { bookingId, _ ->
                        navController.navigate(NavDestination.LiveTracking(bookingId = bookingId)) {
                            popUpTo(NavDestination.Home) { inclusive = false }
                        }
                    }
                )
            } else {
                CheckoutCarScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onConfirmBooking = { bookingId, _ ->
                        navController.navigate(NavDestination.LiveTracking(bookingId = bookingId)) {
                            popUpTo(NavDestination.Home) { inclusive = false }
                        }
                    }
                )
            }
        }

        // --- Booking Flow ---
        composable<NavDestination.CheckoutCar> { backStackEntry ->
            val route = backStackEntry.toRoute<NavDestination.CheckoutCar>()
            CheckoutCarScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onConfirmBooking = { bookingId, seatPosition ->
                    navController.navigate(NavDestination.LiveTracking(bookingId = bookingId)) {
                        popUpTo(NavDestination.Home) { inclusive = false }
                    }
                }
            )
        }

        composable<NavDestination.CheckoutMotor> { backStackEntry ->
            val route = backStackEntry.toRoute<NavDestination.CheckoutMotor>()
            CheckoutMotorScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onConfirmBooking = { bookingId, helmetChoice ->
                    navController.navigate(NavDestination.LiveTracking(bookingId = bookingId)) {
                        popUpTo(NavDestination.Home) { inclusive = false }
                    }
                }
            )
        }

        composable<NavDestination.Payment> { backStackEntry ->
            val route = backStackEntry.toRoute<NavDestination.Payment>()
            LaunchedEffect(Unit) {
                navController.navigate(NavDestination.LiveTracking(bookingId = route.bookingId)) {
                    popUpTo(NavDestination.Home) { inclusive = false }
                }
            }
        }

        composable<NavDestination.QrisPayment> { backStackEntry ->
            val route = backStackEntry.toRoute<NavDestination.QrisPayment>()
            LaunchedEffect(Unit) {
                navController.navigate(NavDestination.LiveTracking(bookingId = route.bookingId)) {
                    popUpTo(NavDestination.Home) { inclusive = false }
                }
            }
        }

        composable<NavDestination.LiveTracking>(
            deepLinks = listOf(navDeepLink { uriPattern = "nebeng://trip/{bookingId}/tracking" })
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<NavDestination.LiveTracking>()
            LiveTrackingScreen(
                onNavigateBack = {
                    navController.navigate(NavDestination.Home) {
                        popUpTo(NavDestination.Home) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToChat = { driverName, bookingId, vehicleInfo, pin ->
                    navController.navigate(
                        NavDestination.ChatDetail(
                            driverName = driverName,
                            vehicleInfo = vehicleInfo,
                            pin = pin,
                            bookingId = bookingId,
                            isTripCompleted = false
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

        composable<NavDestination.TripDone>(
            deepLinks = listOf(navDeepLink { uriPattern = "nebeng://trip/{bookingId}/done" })
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<NavDestination.TripDone>()
            TripDoneScreen(
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
                },
                onNavigateToTip = { bookingId ->
                    navController.navigate(NavDestination.Tip(bookingId = bookingId))
                }
            )
        }

        composable<NavDestination.Tip>(
            deepLinks = listOf(
                navDeepLink { uriPattern = "nebeng://trip/{bookingId}/tip" },
                navDeepLink { uriPattern = "nebeng://tip/{bookingId}" }
            )
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<NavDestination.Tip>()
            TipScreen(
                onSkip = {
                    navController.navigate(NavDestination.Home) {
                        popUpTo(NavDestination.Home) { inclusive = true }
                    }
                },
                onDone = {
                    navController.navigate(NavDestination.Home) {
                        popUpTo(NavDestination.Home) { inclusive = true }
                    }
                }
            )
        }
        composable<NavDestination.VehicleManagement> {
            VehicleManagementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable<NavDestination.RoutineCommute> {
            RoutineCommuteScreen(
                onNavigateBack = { navController.popBackStack() },
                onSearchRide = { origin, destination, vehicleType, pLat, pLng ->
                    navController.navigate(
                        NavDestination.SearchResults(
                            pickupAddress = origin,
                            dropoffAddress = destination,
                            vehicleType = vehicleType,
                            pickupLat = pLat,
                            pickupLng = pLng
                        )
                    )
                }
            )
        }
    }
}
