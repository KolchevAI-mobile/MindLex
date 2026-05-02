package com.example.mindlex.data.remote.supabase

import com.example.mindlex.data.remote.supabase.models.ClozeExerciseDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ClozeRemoteDataSource(
    private val client: SupabaseClient
) {

    private val tableName = "cloze_exercises"

    suspend fun getAllExercises(): List<ClozeExerciseDto> = withContext(Dispatchers.IO) {
        client.postgrest.from(tableName)
            .select()
            .decodeList<ClozeExerciseDto>()
    }

    suspend fun safeGetAllExercises(): Result<List<ClozeExerciseDto>> = runCatching {
        getAllExercises()
    }
}
