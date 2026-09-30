package com.disinidev.nebeng.presentation.tracking

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.maplibre.android.geometry.LatLng
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

@Serializable
private data class OsrmResponse(
    val code: String = "",
    val routes: List<OsrmRouteItem> = emptyList()
)

@Serializable
private data class OsrmRouteItem(
    val geometry: OsrmGeometryData? = null
)

@Serializable
private data class OsrmGeometryData(
    val coordinates: List<List<Double>> = emptyList()
)

object RoutePointsHelper {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Curated real road waypoints following streets in Jakarta:
     * Jl. Pakubuwono -> Jl. Bumi -> Jl. Hang Lekir -> Jl. Prof. Dr. Moestopo (Pickup)
     */
    val defaultApproachRoadPoints: List<LatLng> = listOf(
        LatLng(-6.22450, 106.80480), // Start driver
        LatLng(-6.22415, 106.80502), // Jl. Pakubuwono VI
        LatLng(-6.22365, 106.80535), // Belok Jl. Bumi
        LatLng(-6.22305, 106.80572), // Masuk Jl. Hang Lekir
        LatLng(-6.22250, 106.80608), // Pertigaan Moestopo
        LatLng(-6.22200, 106.80632), // Jl. Prof. Dr. Moestopo
        LatLng(-6.22150, 106.80650)  // Titik Jemput: Pintu Barat Lawson
    )

    /**
     * Curated real road waypoints following streets in Jakarta:
     * Jl. Prof. Dr. Moestopo -> Jl. Asia Afrika -> Bundaran Senayan -> Jl. Jenderal Sudirman -> Kawasan SCBD Lot 8
     */
    val defaultDestRoadPoints: List<LatLng> = listOf(
        LatLng(-6.22150, 106.80650), // Titik Jemput: Pintu Barat Lawson
        LatLng(-6.22105, 106.80702), // Jl. Prof. Dr. Moestopo
        LatLng(-6.22055, 106.80755), // Masuk simpang Bundaran Pemuda
        LatLng(-6.22010, 106.80805), // Jl. Asia Afrika
        LatLng(-6.21952, 106.80880), // Bundaran Senayan
        LatLng(-6.21920, 106.80922), // Masuk Jl. Jenderal Sudirman
        LatLng(-6.21880, 106.80970), // Depan FX Sudirman
        LatLng(-6.21845, 106.81008), // Belok gerbang SCBD
        LatLng(-6.21820, 106.81030), // Kawasan Bisnis SCBD
        LatLng(-6.21800, 106.81050)  // SCBD Lot 8 (Tujuan)
    )

    /**
     * Fetches exact road geometry from OpenStreetMap OSRM routing engine.
     * Falls back to high-resolution road waypoints if offline or network unavailable.
     */
    suspend fun getRoadRoute(
        start: LatLng,
        end: LatLng,
        defaultFallback: List<LatLng> = emptyList()
    ): List<LatLng> = withContext(Dispatchers.IO) {
        try {
            val urlStr = String.format(
                Locale.US,
                "https://router.project-osrm.org/route/v1/driving/%.6f,%.6f;%.6f,%.6f?overview=full&geometries=geojson",
                start.longitude, start.latitude,
                end.longitude, end.latitude
            )
            val url = URL(urlStr)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 3000
                readTimeout = 3000
                setRequestProperty("User-Agent", "NebengCarpool/1.0 (Android; FreeCarpool)")
            }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val response = conn.inputStream.bufferedReader().use(BufferedReader::readText)
                val osrm = json.decodeFromString<OsrmResponse>(response)
                val coords = osrm.routes.firstOrNull()?.geometry?.coordinates
                if (!coords.isNullOrEmpty()) {
                    return@withContext coords.map { pair ->
                        // GeoJSON format is [longitude, latitude]
                        LatLng(pair[1], pair[0])
                    }
                }
            }
        } catch (_: Exception) {
            // Network fallback
        }

        if (defaultFallback.isNotEmpty()) {
            defaultFallback
        } else {
            listOf(start, end)
        }
    }
}
