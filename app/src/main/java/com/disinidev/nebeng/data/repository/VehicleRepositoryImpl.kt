package com.disinidev.nebeng.data.repository

import android.util.Log
import com.disinidev.nebeng.domain.model.VehicleInfo
import com.disinidev.nebeng.domain.model.VehicleType
import com.disinidev.nebeng.domain.repository.VehicleRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
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
    private val supabaseClient: SupabaseClient
) : VehicleRepository {

    private val localVehicles = ConcurrentHashMap<String, MutableList<VehicleInfo>>()

    override suspend fun getDriverVehicles(driverId: String): Result<List<VehicleInfo>> = withContext(Dispatchers.IO) {
        runCatching {
            try {
                val list = supabaseClient.from("vehicle_registrations").select {
                    filter {
                        eq("driver_id", driverId)
                    }
                }.decodeList<RemoteVehicleDto>()

                val vehicles = list.map { it.toDomain() }
                localVehicles[driverId] = vehicles.toMutableList()
                return@runCatching vehicles
            } catch (e: Exception) {
                Log.e("VehicleRepository", "getDriverVehicles Supabase error: ${e.message}", e)
            }

            localVehicles[driverId] ?: emptyList()
        }
    }

    override suspend fun addVehicle(
        driverId: String,
        vehicle: VehicleInfo
    ): Result<VehicleInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val newId = UUID.randomUUID().toString()
            val domainWithId = vehicle.copy(id = newId)

            val current = localVehicles.getOrPut(driverId) { mutableListOf() }
            current.add(domainWithId)

            try {
                val payload = buildMap<String, Any?> {
                    put("id", newId)
                    put("driver_id", driverId)
                    put("brand", vehicle.brand)
                    put("model", vehicle.model)
                    put("plate", vehicle.plate.uppercase())
                    put("type", if (vehicle.type == VehicleType.MOTORCYCLE) "motorcycle" else "car")
                    if (vehicle.color != null) put("color", vehicle.color)
                    if (vehicle.year != null) put("year", vehicle.year)
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
            localVehicles.values.forEach { list ->
                list.removeAll { it.id == vehicleId }
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
}
