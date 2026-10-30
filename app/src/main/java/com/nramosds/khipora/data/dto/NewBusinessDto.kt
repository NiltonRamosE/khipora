package com.nramosds.khipora.data.dto

import com.google.gson.annotations.SerializedName

data class NewBusinessDto(
    @SerializedName("nombre") val name: String
)
