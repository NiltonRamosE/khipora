package com.nramosds.khipora.core.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.khiporaDataStore by preferencesDataStore(name = "khipora_config")

class BusinessPreferences(private val context: Context) {
    private val businessIdKey = longPreferencesKey("negocio_id")
    private val businessNameKey = stringPreferencesKey("negocio_nombre")
    val businessName: Flow<String> =
        context.khiporaDataStore.data.map { it[businessNameKey].orEmpty() }
    val businessId: Flow<Long> = context.khiporaDataStore.data.map { preferences ->
        preferences[businessIdKey] ?: 0L
    }

    suspend fun saveBusinessId(id: Long) {
        context.khiporaDataStore.edit { it[businessIdKey] = id }
    }

    suspend fun saveBusiness(id: Long, name: String) {
        context.khiporaDataStore.edit {
            it[businessIdKey] = id
            it[businessNameKey] = name
        }
    }

    suspend fun updateName(id: Long, name: String) {
        context.khiporaDataStore.edit {
            if (it[businessIdKey] == id) it[businessNameKey] = name
        }
    }
}
