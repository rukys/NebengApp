package com.disinidev.nebeng.data.repository

import android.content.Context
import com.disinidev.nebeng.domain.model.RoutineCommute
import com.disinidev.nebeng.domain.repository.RoutineRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Serializable
private data class RemoteRoutineCommuteDto(
    val id: String,
    val user_id: String,
    val origin_name: String,
    val destination_name: String,
    val origin_lat: Double = 0.0,
    val origin_lng: Double = 0.0,
    val destination_lat: Double = 0.0,
    val destination_lng: Double = 0.0,
    val departure_time: String,
    val active_days: String,
    val vehicle_type: String = "car",
    val is_enabled: Boolean = true,
    val auto_book: Boolean = false,
    val created_at: String? = null
) {
    fun toDomain(): RoutineCommute {
        val daysList = active_days.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .ifEmpty { listOf(1, 2, 3, 4, 5) }

        return RoutineCommute(
            id = id,
            userId = user_id,
            originName = origin_name,
            destinationName = destination_name,
            originLat = origin_lat,
            originLng = origin_lng,
            destinationLat = destination_lat,
            destinationLng = destination_lng,
            departureTime = departure_time,
            activeDays = daysList,
            vehicleType = vehicle_type,
            isEnabled = is_enabled,
            autoBook = auto_book,
            createdAt = created_at
        )
    }
}

@Singleton
class RoutineRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    @ApplicationContext context: Context
) : RoutineRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private fun getLocalRoutines(userId: String): MutableList<RoutineCommute> {
        val raw = prefs.getString(KEY_PREFIX + userId, null) ?: return mutableListOf()
        return try {
            json.decodeFromString<List<RoutineCommute>>(raw).toMutableList()
        } catch (_: Exception) {
            mutableListOf()
        }
    }

    private fun saveLocalRoutines(userId: String, list: List<RoutineCommute>) {
        prefs.edit().putString(KEY_PREFIX + userId, json.encodeToString(list)).apply()
    }

    override suspend fun getRoutineCommutes(userId: String): Result<List<RoutineCommute>> = withContext(Dispatchers.IO) {
        runCatching {
            val localList = getLocalRoutines(userId)

            try {
                val remoteList = supabaseClient.from("routine_commutes").select {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeList<RemoteRoutineCommuteDto>()

                val remoteCommutes = remoteList.map { it.toDomain() }

                // Merge remote with local items
                val remoteIds = remoteCommutes.map { it.id }.toSet()
                val merged = remoteCommutes.toMutableList()
                for (local in localList) {
                    if (local.id !in remoteIds) {
                        merged.add(local)
                    }
                }

                saveLocalRoutines(userId, merged)
                return@runCatching merged
            } catch (e: Exception) {
                Timber.e(e, "getRoutineCommutes Supabase error: ${e.message}")
            }

            localList
        }
    }

    override suspend fun saveRoutineCommute(commute: RoutineCommute): Result<RoutineCommute> = withContext(Dispatchers.IO) {
        runCatching {
            val finalId = if (commute.id.isBlank()) UUID.randomUUID().toString() else commute.id
            val finalCommute = commute.copy(
                id = finalId,
                createdAt = commute.createdAt ?: Instant.now().toString()
            )

            // 1. Save locally immediately
            val list = getLocalRoutines(commute.userId)
            list.removeAll { it.id == finalId }
            list.add(0, finalCommute)
            saveLocalRoutines(commute.userId, list)

            // 2. Sync to Supabase
            try {
                val payload = buildMap<String, Any?> {
                    put("id", finalId)
                    put("user_id", commute.userId)
                    put("origin_name", commute.originName)
                    put("destination_name", commute.destinationName)
                    put("origin_lat", commute.originLat)
                    put("origin_lng", commute.originLng)
                    put("destination_lat", commute.destinationLat)
                    put("destination_lng", commute.destinationLng)
                    put("departure_time", commute.departureTime)
                    put("active_days", commute.activeDays.joinToString(","))
                    put("vehicle_type", commute.vehicleType)
                    put("is_enabled", commute.isEnabled)
                    put("auto_book", commute.autoBook)
                }
                supabaseClient.from("routine_commutes").upsert(payload)
            } catch (e: Exception) {
                Timber.e(e, "saveRoutineCommute Supabase error: ${e.message}")
            }

            finalCommute
        }
    }

    override suspend fun toggleRoutineCommute(id: String, isEnabled: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // Update local state in all cached user profiles
            val allKeys = prefs.all.keys.filter { it.startsWith(KEY_PREFIX) }
            for (key in allKeys) {
                val raw = prefs.getString(key, null) ?: continue
                val items = try {
                    json.decodeFromString<List<RoutineCommute>>(raw).toMutableList()
                } catch (_: Exception) {
                    continue
                }
                val index = items.indexOfFirst { it.id == id }
                if (index != -1) {
                    items[index] = items[index].copy(isEnabled = isEnabled)
                    prefs.edit().putString(key, json.encodeToString(items)).apply()
                }
            }

            // Sync to Supabase
            try {
                supabaseClient.from("routine_commutes").update(
                    mapOf("is_enabled" to isEnabled)
                ) {
                    filter { eq("id", id) }
                }
            } catch (e: Exception) {
                Timber.e(e, "toggleRoutineCommute Supabase error: ${e.message}")
            }
            Unit
        }
    }

    override suspend fun deleteRoutineCommute(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // Remove locally
            val allKeys = prefs.all.keys.filter { it.startsWith(KEY_PREFIX) }
            for (key in allKeys) {
                val raw = prefs.getString(key, null) ?: continue
                val items = try {
                    json.decodeFromString<List<RoutineCommute>>(raw).toMutableList()
                } catch (_: Exception) {
                    continue
                }
                if (items.removeAll { it.id == id }) {
                    prefs.edit().putString(key, json.encodeToString(items)).apply()
                }
            }

            // Sync to Supabase
            try {
                supabaseClient.from("routine_commutes").delete {
                    filter { eq("id", id) }
                }
            } catch (e: Exception) {
                Timber.e(e, "deleteRoutineCommute Supabase error: ${e.message}")
            }
            Unit
        }
    }

    companion object {
        private const val PREFS_NAME = "nebeng_routine_commutes_prefs"
        private const val KEY_PREFIX = "routines_"
    }
}
