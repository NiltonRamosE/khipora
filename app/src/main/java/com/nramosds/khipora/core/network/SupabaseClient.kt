package com.nramosds.khipora.core.network

import com.nramosds.khipora.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SupabaseClient {
    fun create(): SupabaseApi {
        require(BuildConfig.SUPABASE_URL.startsWith("https://")) { "SUPABASE_URL debe usar HTTPS" }
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("apikey", BuildConfig.SUPABASE_PUBLISHABLE_KEY)
                        .header("Accept", "application/json")
                        .build()
                )
            }
            .build()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.SUPABASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseApi::class.java)
    }
}
