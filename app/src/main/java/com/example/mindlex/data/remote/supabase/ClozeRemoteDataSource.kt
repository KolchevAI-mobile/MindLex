package com.example.mindlex.data.remote.supabase

import com.example.mindlex.data.remote.supabase.models.ClozeExerciseDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/** Удалённая загрузка упражнений contextual cloze из Supabase. */
class ClozeRemoteDataSource(
    private val client: SupabaseClient
) {

    private val tableName = "cloze_exercises"

    suspend fun getAllExercises(): List<ClozeExerciseDto> = withContext(Dispatchers.IO) {
        Timber.d("[ClozeRemote] Запрос к таблице '$tableName'")
        val list = client.postgrest.from(tableName)
            .select()
            .decodeList<ClozeExerciseDto>()
        Timber.d("[ClozeRemote] Загружено ${list.size} упражнений")
        list
    }

    suspend fun safeGetAllExercises(): Result<List<ClozeExerciseDto>> = runCatching {
        getAllExercises()
    }.onFailure { e ->
        Timber.e(e, "[ClozeRemote] Ошибка загрузки cloze_exercises")
    }
}
