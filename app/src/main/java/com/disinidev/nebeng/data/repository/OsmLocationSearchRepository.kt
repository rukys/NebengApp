package com.disinidev.nebeng.data.repository

import android.content.Context
import android.location.Geocoder
import com.disinidev.nebeng.data.model.OsmNominatimPlace
import com.disinidev.nebeng.domain.model.PlaceSuggestion
import com.disinidev.nebeng.domain.repository.LocationSearchRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OsmLocationSearchRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) : LocationSearchRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override suspend fun searchPlaces(query: String): List<PlaceSuggestion> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return@withContext emptyList()

        try {
            val networkResults = fetchFromNominatim(trimmed)
            if (networkResults.isNotEmpty()) {
                return@withContext networkResults
            }
        } catch (_: Exception) {
            // Network fail -> fallback
        }

        // Fallback 1: Android native Geocoder
        try {
            val geocoderResults = fetchFromGeocoder(trimmed)
            if (geocoderResults.isNotEmpty()) {
                return@withContext geocoderResults
            }
        } catch (_: Exception) {
            // Geocoder fail -> fallback to local spots
        }

        // Fallback 2: Local curated spots
        searchLocalCuratedSpots(trimmed)
    }

    private fun fetchFromNominatim(query: String): List<PlaceSuggestion> {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val urlString = "https://nominatim.openstreetmap.org/search?q=$encoded&format=json&limit=6&countrycodes=id"
        val url = URL(urlString)

        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("User-Agent", "NebengApp/1.0 (Android; dev@nebeng.id)")
            setRequestProperty("Accept", "application/json")
            connectTimeout = 4000
            readTimeout = 4000
        }

        return try {
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                val places = json.decodeFromString<List<OsmNominatimPlace>>(response)

                places.map { place ->
                    val segments = place.displayName.split(",").map { it.trim() }
                    val primaryName = if (!place.name.isNullOrBlank()) {
                        place.name
                    } else {
                        segments.firstOrNull() ?: query
                    }
                    val secondaryText = if (segments.size > 1) {
                        segments.drop(1).take(3).joinToString(", ")
                    } else {
                        place.displayName
                    }

                    PlaceSuggestion(
                        id = place.placeId.toString(),
                        name = primaryName,
                        fullAddress = secondaryText,
                        latitude = place.lat.toDoubleOrNull() ?: -6.2088,
                        longitude = place.lon.toDoubleOrNull() ?: 106.8456
                    )
                }
            } else {
                emptyList()
            }
        } finally {
            connection.disconnect()
        }
    }

    @Suppress("DEPRECATION")
    private fun fetchFromGeocoder(query: String): List<PlaceSuggestion> {
        val geocoder = Geocoder(context, Locale("id", "ID"))
        val addresses = geocoder.getFromLocationName(query, 5) ?: return emptyList()

        return addresses.mapIndexed { index, address ->
            val primaryName = address.featureName ?: address.thoroughfare ?: query
            val secondaryText = buildString {
                if (!address.subLocality.isNullOrBlank()) append(address.subLocality).append(", ")
                if (!address.locality.isNullOrBlank()) append(address.locality).append(", ")
                if (!address.subAdminArea.isNullOrBlank()) append(address.subAdminArea)
            }.trimEnd(',', ' ')

            PlaceSuggestion(
                id = "geo_$index",
                name = primaryName,
                fullAddress = secondaryText.ifBlank { address.getAddressLine(0) ?: query },
                latitude = address.latitude,
                longitude = address.longitude
            )
        }
    }

    private fun searchLocalCuratedSpots(query: String): List<PlaceSuggestion> {
        val curated = listOf(
            PlaceSuggestion("c1", "Stasiun Tebet (Pintu Barat)", "Jl. Tebet Raya, Tebet Timur, Jakarta Selatan", -6.2297, 106.8580),
            PlaceSuggestion("c2", "SCBD Sudirman (Lot 8 & Pasific)", "Jl. Jend. Sudirman Kav. 52-53, Jakarta Selatan", -6.2274, 106.8080),
            PlaceSuggestion("c3", "Stasiun MRT Fatmawati", "Cilandak Barat, Cilandak, Jakarta Selatan", -6.2926, 106.7972),
            PlaceSuggestion("c4", "Kuningan City", "Jl. Prof. DR. Satrio Kav. 18, Karet Kuningan, Jakarta Selatan", -6.2248, 106.8299),
            PlaceSuggestion("c5", "Stasiun Manggarai", "Manggarai, Tebet, Jakarta Selatan", -6.2099, 106.8499),
            PlaceSuggestion("c6", "Mega Kuningan", "Kawasan Mega Kuningan, Setiabudi, Jakarta Selatan", -6.2289, 106.8268),
            PlaceSuggestion("c7", "Cilandak Town Square", "Jl. TB Simatupang, Cilandak, Jakarta Selatan", -6.2917, 106.7986),
            PlaceSuggestion("c8", "Stasiun Sudirman", "Dukuh Atas, Menteng, Jakarta Pusat", -6.2023, 106.8236)
        )

        val lower = query.lowercase()
        return curated.filter {
            it.name.lowercase().contains(lower) || it.fullAddress.lowercase().contains(lower)
        }
    }
}
