package com.example.mindlex.data.remote.supabase

import com.example.mindlex.data.remote.supabase.models.SynonymChainDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/** Удалённая загрузка шагов механики «Цепочка синонимов». */
class SynonymChainRemoteDataSource(
    private val client: SupabaseClient
) {
    private val tableName = "synonym_chains"

    suspend fun getAllSteps(): List<SynonymChainDto> = withContext(Dispatchers.IO) {
        Timber.d("[SynonymChainRemote] Запрос к таблице '$tableName'")
        val list = client.postgrest.from(tableName)
            .select()
            .decodeList<SynonymChainDto>()
        Timber.d("[SynonymChainRemote] Загружено ${list.size} шагов")
        list
    }

    suspend fun safeGetAllSteps(): Result<List<SynonymChainDto>> = runCatching {
        getAllSteps()
    }.onFailure { e ->
        Timber.e(e, "[SynonymChainRemote] Ошибка загрузки synonym_chains")
    }
}
