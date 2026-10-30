package com.nramosds.khipora

import android.content.Context
import com.nramosds.khipora.core.network.SupabaseClient
import com.nramosds.khipora.core.preferences.BusinessPreferences
import com.nramosds.khipora.data.repository.BusinessRepositoryImpl
import com.nramosds.khipora.data.repository.ClosingRepositoryImpl
import com.nramosds.khipora.domain.repository.BusinessRepository
import com.nramosds.khipora.domain.repository.ClosingRepository

class AppContainer(context: Context) {
    private val api = SupabaseClient.create()
    val businessPreferences = BusinessPreferences(context.applicationContext)
    val businessRepository: BusinessRepository = BusinessRepositoryImpl(api)
    val closingRepository: ClosingRepository = ClosingRepositoryImpl(api)
}
