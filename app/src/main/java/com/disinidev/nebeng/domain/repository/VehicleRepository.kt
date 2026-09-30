package com.disinidev.nebeng.domain.repository

import com.disinidev.nebeng.domain.model.VehicleInfo

interface VehicleRepository {
    suspend fun getDriverVehicles(driverId: String): Result<List<VehicleInfo>>
    suspend fun addVehicle(driverId: String, vehicle: VehicleInfo): Result<VehicleInfo>
    suspend fun deleteVehicle(vehicleId: String): Result<Unit>
}
