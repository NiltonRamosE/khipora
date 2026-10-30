package com.nramosds.khipora.data.dto

import com.google.gson.annotations.SerializedName

data class BusinessDto(
    val id: Long,
    @SerializedName("nombre") val name: String
)
