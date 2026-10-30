package com.nramosds.khipora.domain.repository

import com.nramosds.khipora.domain.model.Business

interface BusinessRepository {
    suspend fun getById(id: Long): Business?
    suspend fun create(name: String): Business
    suspend fun update(id: Long, name: String): Business
}
