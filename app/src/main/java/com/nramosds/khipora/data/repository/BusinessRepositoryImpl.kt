package com.nramosds.khipora.data.repository

import com.nramosds.khipora.core.network.SupabaseApi
import com.nramosds.khipora.data.dto.NewBusinessDto
import com.nramosds.khipora.domain.model.Business
import com.nramosds.khipora.domain.repository.BusinessRepository

class BusinessRepositoryImpl(private val api: SupabaseApi) : BusinessRepository {

    override suspend fun getById(id: Long): Business? {
        return api.getBusinesses(id = "eq.$id").firstOrNull()?.let { business ->
            Business(id = business.id, name = business.name)
        }
    }

    override suspend fun create(name: String): Business {
        val business = api.createBusiness(NewBusinessDto(name)).single()
        return Business(id = business.id, name = business.name)
    }

    override suspend fun update(id: Long, name: String): Business {
        val business = api.updateBusiness("eq.$id", NewBusinessDto(name)).single()
        return Business(id = business.id, name = business.name)
    }
}
