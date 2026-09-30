package com.disinidev.nebeng.data.repository

import android.content.Context
import android.util.Log
import com.disinidev.nebeng.domain.model.VehicleInfo
import com.disinidev.nebeng.domain.model.VehicleType
import com.disinidev.nebeng.domain.repository.VehicleRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class RemoteVehicleDto(
    val id: String,
    val driver_id: String,
    val brand: String,
    val model: String,
    val plate: String,
    val type: String,
    val color: String? = null,
    val year: Int? = null,
    val is_verified: Boolean = false
) {
    fun toDomain(): VehicleInfo = VehicleInfo(
        id = id,
        brand = brand,
        model = model,
        plate = plate,
        type = if (type == "motorcycle") VehicleType.MOTORCYCLE else VehicleType.CAR,
        color = color,
        year = year,
        isVerified = is_verified
    )
}

@Singleton
class VehicleRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    @ApplicationContext context: Context
) : VehicleRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private fun getPersistedVehicles(driverId: String): MutableList<VehicleInfo> {
        val raw = prefs.getString(KEY_PREFIX + driverId, null) ?: return mutableListOf()
        return try {
            json.decodeFromString<List<VehicleInfo>>(raw).toMutableList()
        } catch (_: Exception) {
            mutableListOf()
        }
    }

    private fun savePersistedVehicles(driverId: String, list: List<VehicleInfo>) {
        try {
            val raw = json.encodeToString(list)
            prefs.edit().putString(KEY_PREFIX + driverId, raw).apply()
        } catch (e: Exception) {
            Log.e("VehicleRepository", "savePersistedVehicles error: ${e.message}", e)
        }
    }

    override suspend fun getDriverVehicles(driverId: String): Result<List<VehicleInfo>> = withContext(Dispatchers.IO) {
        runCatching {
            val localList = getPersistedVehicles(driverId)

            try {
                val list = supabaseClient.from("vehicle_registrations").select {
                    filter {
                        eq("driver_id", driverId)
                    }
                }.decodeList<RemoteVehicleDto>()

                val remoteVehicles = list.map { it.toDomain() }

                // Merge remote with local list:
                // Keep all remote items, and keep any locally-saved item not present in remote
                val remotePlates = remoteVehicles.map { it.plate.uppercase() }.toSet()
                val merged = remoteVehicles.toMutableList()
                for (local in localList) {
                    if (local.plate.uppercase() !in remotePlates) {
                        merged.add(local)
                    }
                }

                savePersistedVehicles(driverId, merged)
                return@runCatching merged
            } catch (e: Exception) {
                Log.e("VehicleRepository", "getDriverVehicles Supabase error: ${e.message}", e)
            }

            localList
        }
    }

    override suspend fun addVehicle(
        driverId: String,
        vehicle: VehicleInfo
    ): Result<VehicleInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val newId = vehicle.id ?: UUID.randomUUID().toString()
            val domainWithId = vehicle.copy(id = newId, isVerified = true)

            // 1. Immediately persist locally to never lose data across app restarts/navigation
            val current = getPersistedVehicles(driverId)
            current.removeAll { it.plate.equals(vehicle.plate, ignoreCase = true) }
            current.add(domainWithId)
            savePersistedVehicles(driverId, current)

            // 2. Sync to Supabase in background
            try {
                val payload = buildMap<String, Any?> {
                    put("id", newId)
                    put("driver_id", driverId)
                    put("brand", vehicle.brand)
                    put("model", vehicle.model)
                    put("plate", vehicle.plate.uppercase())
                    put("type", if (vehicle.type == VehicleType.MOTORCYCLE) "motorcycle" else "car")
                    put("color", vehicle.color?.ifBlank { null } ?: "Hitam")
                    put("year", vehicle.year ?: 2022)
                    put("is_verified", true)
                }
                supabaseClient.from("vehicle_registrations").insert(payload)
            } catch (e: Exception) {
                Log.e("VehicleRepository", "addVehicle Supabase error: ${e.message}", e)
            }

            domainWithId
        }
    }

    override suspend fun deleteVehicle(vehicleId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // Delete from all cached driver keys in SharedPreferences
            prefs.all.keys.filter { it.startsWith(KEY_PREFIX) }.forEach { key ->
                val raw = prefs.getString(key, null)
                if (raw != null) {
                    try {
                        val list = json.decodeFromString<List<VehicleInfo>>(raw).toMutableList()
                        if (list.removeAll { it.id == vehicleId }) {
                            prefs.edit().putString(key, json.encodeToString(list)).apply()
                        }
                    } catch (_: Exception) {}
                }
            }

            try {
                supabaseClient.from("vehicle_registrations").delete {
                    filter {
                        eq("id", vehicleId)
                    }
                }
            } catch (e: Exception) {
                Log.e("VehicleRepository", "deleteVehicle Supabase error: ${e.message}", e)
            }
            Unit
        }
    }

    companion object {
        private const val PREFS_NAME = "nebeng_vehicles"
        private const val KEY_PREFIX = "vehicles_"
    }
}
