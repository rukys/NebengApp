package com.disinidev.nebeng.presentation.tracking

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.disinidev.nebeng.R
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style

data class ProjectedTrackingPoints(
    val driverX: Float,
    val driverY: Float,
    val pickupX: Float,
    val pickupY: Float,
    val destX: Float,
    val destY: Float,
    val approachRoutePoints: List<Offset> = emptyList(),
    val destRoutePoints: List<Offset> = emptyList()
)

@Composable
fun TrackingMapView(
    driverLocation: LatLng,
    pickupLocation: LatLng,
    destLocation: LatLng,
    onPointsProjected: (ProjectedTrackingPoints) -> Unit,
    modifier: Modifier = Modifier,
    recenterTrigger: Int = 0
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isMapReady by remember { mutableStateOf(false) }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }

    val currentDriverLocation by rememberUpdatedState(driverLocation)
    val currentPickupLocation by rememberUpdatedState(pickupLocation)
    val currentDestLocation by rememberUpdatedState(destLocation)
    val currentOnPointsProjected by rememberUpdatedState(onPointsProjected)

    val isNearSenayan = Math.abs(pickupLocation.latitude - (-6.2215)) < 0.03 &&
            Math.abs(pickupLocation.longitude - 106.8065) < 0.03

    var approachRoadCoords by remember {
        mutableStateOf(
            if (isNearSenayan) RoutePointsHelper.defaultApproachRoadPoints
            else listOf(driverLocation, pickupLocation)
        )
    }
    var destRoadCoords by remember {
        mutableStateOf(
            if (isNearSenayan) RoutePointsHelper.defaultDestRoadPoints
            else listOf(pickupLocation, destLocation)
        )
    }

    val currentApproachRoadCoords by rememberUpdatedState(approachRoadCoords)
    val currentDestRoadCoords by rememberUpdatedState(destRoadCoords)

    // Fetch turn-by-turn road geometry from OSRM whenever endpoints change
    LaunchedEffect(driverLocation, pickupLocation, destLocation) {
        val nearSenayan = Math.abs(pickupLocation.latitude - (-6.2215)) < 0.03 &&
                Math.abs(pickupLocation.longitude - 106.8065) < 0.03

        val approachFallback = if (nearSenayan) RoutePointsHelper.defaultApproachRoadPoints
                               else listOf(driverLocation, pickupLocation)
        val destFallback = if (nearSenayan) RoutePointsHelper.defaultDestRoadPoints
                           else listOf(pickupLocation, destLocation)

        val approach = RoutePointsHelper.getRoadRoute(
            start = driverLocation,
            end = pickupLocation,
            defaultFallback = approachFallback
        )
        val dest = RoutePointsHelper.getRoadRoute(
            start = pickupLocation,
            end = destLocation,
            defaultFallback = destFallback
        )
        approachRoadCoords = approach
        destRoadCoords = dest
    }

    val updateProjections: () -> Unit = {
        mapLibreMap?.let { map ->
            if (isMapReady) {
                val proj = map.projection
                val driverPoint = proj.toScreenLocation(currentDriverLocation)
                val pickupPoint = proj.toScreenLocation(currentPickupLocation)
                val destPoint = proj.toScreenLocation(currentDestLocation)

                val projectedApproach = currentApproachRoadCoords.map {
                    val p = proj.toScreenLocation(it)
                    Offset(p.x, p.y)
                }
                val projectedDest = currentDestRoadCoords.map {
                    val p = proj.toScreenLocation(it)
                    Offset(p.x, p.y)
                }

                currentOnPointsProjected(
                    ProjectedTrackingPoints(
                        driverX = driverPoint.x,
                        driverY = driverPoint.y,
                        pickupX = pickupPoint.x,
                        pickupY = pickupPoint.y,
                        destX = destPoint.x,
                        destY = destPoint.y,
                        approachRoutePoints = projectedApproach,
                        destRoutePoints = projectedDest
                    )
                )
            }
        }
    }

    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
            addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                updateProjections()
            }
            getMapAsync { map ->
                mapLibreMap = map
                map.uiSettings.apply {
                    isAttributionEnabled = false
                    isLogoEnabled = false
                    isCompassEnabled = false
                    isRotateGesturesEnabled = true
                    isTiltGesturesEnabled = false
                }

                map.addOnCameraMoveListener {
                    updateProjections()
                }
                map.addOnCameraIdleListener {
                    updateProjections()
                }

                // Use Carto Positron (clean OpenStreetMap vector tiles, free & fast)
                val styleUrl = "https://basemaps.cartocdn.com/gl/positron-gl-style/style.json"
                map.setStyle(Style.Builder().fromUri(styleUrl)) {
                    isMapReady = true
                    updateProjections()
                }
            }
        }
    }

    val animateToFitBounds: () -> Unit = {
        mapLibreMap?.let { map ->
            if (isMapReady) {
                try {
                    val pLoc = currentPickupLocation
                    val dLoc = currentDriverLocation
                    val latDiff = Math.abs(dLoc.latitude - pLoc.latitude)
                    val lngDiff = Math.abs(dLoc.longitude - pLoc.longitude)

                    val density = context.resources.displayMetrics.density
                    val padTop = (90 * density).toInt()
                    val padBottom = (250 * density).toInt()
                    val padSide = (60 * density).toInt()

                    if (latDiff < 0.0002 && lngDiff < 0.0002) {
                        map.animateCamera(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition.Builder()
                                    .target(pLoc)
                                    .zoom(16.2)
                                    .build()
                            ),
                            1000
                        )
                    } else {
                        val bounds = LatLngBounds.Builder()
                            .include(dLoc)
                            .include(pLoc)
                            .build()

                        map.animateCamera(
                            CameraUpdateFactory.newLatLngBounds(bounds, padSide, padTop, padSide, padBottom),
                            1000
                        )
                    }
                } catch (_: Exception) {
                    map.animateCamera(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.Builder()
                                .target(currentPickupLocation)
                                .zoom(15.5)
                                .build()
                        ),
                        1000
                    )
                }
            }
        }
    }

    LaunchedEffect(driverLocation, pickupLocation, destLocation, approachRoadCoords, destRoadCoords, isMapReady) {
        updateProjections()
    }

    LaunchedEffect(pickupLocation, isMapReady) {
        if (isMapReady && mapLibreMap != null) {
            animateToFitBounds()
        }
    }

    LaunchedEffect(recenterTrigger) {
        if (recenterTrigger > 0 && isMapReady && mapLibreMap != null) {
            animateToFitBounds()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // High-res map fallback behind while vector tiles load
        if (!isMapReady) {
            Image(
                painter = painterResource(id = R.drawable.bg_live_tracking_map),
                contentDescription = "Map Fallback",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )
    }
}
