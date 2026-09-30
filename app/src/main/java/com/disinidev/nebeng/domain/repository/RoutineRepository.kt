package com.disinidev.nebeng.domain.repository

import com.disinidev.nebeng.domain.model.RoutineCommute

interface RoutineRepository {
    suspend fun getRoutineCommutes(userId: String): Result<List<RoutineCommute>>
    suspend fun saveRoutineCommute(commute: RoutineCommute): Result<RoutineCommute>
    suspend fun toggleRoutineCommute(id: String, isEnabled: Boolean): Result<Unit>
    suspend fun deleteRoutineCommute(id: String): Result<Unit>
}
