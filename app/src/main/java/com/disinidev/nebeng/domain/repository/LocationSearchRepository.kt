package com.disinidev.nebeng.domain.repository

import com.disinidev.nebeng.domain.model.PlaceSuggestion

interface LocationSearchRepository {
    suspend fun searchPlaces(query: String): List<PlaceSuggestion>
}
