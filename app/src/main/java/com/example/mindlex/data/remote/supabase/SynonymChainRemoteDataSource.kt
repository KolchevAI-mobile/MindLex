package com.example.mindlex.data.remote.supabase

import com.example.mindlex.data.remote.supabase.models.SynonymChainDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Удалённая загрузка шагов механики «Цепочка синонимов». */
class SynonymChainRemoteDataSource(
    private val client: SupabaseClient
) {
    private val tableName = "synonym_chains"

    suspend fun getAllSteps(): List<SynonymChainDto> = withContext(Dispatchers.IO) {
        client.postgrest.from(tableName)
            .select()
            .decodeList<SynonymChainDto>()
    }

    suspend fun safeGetAllSteps(): Result<List<SynonymChainDto>> = runCatching {
        getAllSteps()
    }
}
