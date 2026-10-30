package com.nramosds.khipora.core.network

import com.nramosds.khipora.data.dto.BusinessDto
import com.nramosds.khipora.data.dto.CreatedClosingDto
import com.nramosds.khipora.data.dto.DailyClosingDto
import com.nramosds.khipora.data.dto.NewBusinessDto
import com.nramosds.khipora.data.dto.NewDailyClosingDto
import com.nramosds.khipora.data.dto.NewPaymentDto
import com.nramosds.khipora.data.dto.PaymentMethodDto
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseApi {
    @GET("rest/v1/negocios")
    suspend fun getBusinesses(
        @Query("select") select: String = "id,nombre",
        @Query("id") id: String,
    ): List<BusinessDto>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/negocios")
    suspend fun createBusiness(@Body body: NewBusinessDto): List<BusinessDto>

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/negocios")
    suspend fun updateBusiness(
        @Query("id") id: String,
        @Body body: NewBusinessDto,
    ): List<BusinessDto>

    @GET("rest/v1/metodos_pago")
    suspend fun getPaymentMethods(
        @Query("select") select: String = "id,nombre,activo",
        @Query("activo") active: String = "eq.true",
        @Query("order") order: String = "id.asc",
    ): List<PaymentMethodDto>

    @GET("rest/v1/cierres")
    suspend fun closings(
        @Query("select") select: String =
            "id,negocio_id,fecha,cantidad,clientes," +
                "hora_inicio,hora_fin,publico,notas," +
                "motivo_consumo,atributo," +
                "pagos(metodo_id,monto)",
        @Query("negocio_id") businessId: String,
        @Query("order") order: String = "fecha.desc",
    ): List<DailyClosingDto>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/cierres")
    suspend fun createClosing(
        @Body body: NewDailyClosingDto,
        @Query("select") select: String = "id",
    ): List<CreatedClosingDto>

    @Headers("Prefer: return=minimal")
    @POST("rest/v1/pagos")
    suspend fun createPayments(@Body body: List<NewPaymentDto>)

    @Headers("Prefer: return=representation")
    @DELETE("rest/v1/cierres")
    suspend fun deleteClosing(
        @Query("id") id: String,
        @Query("select") select: String = "id",
    ): List<CreatedClosingDto>

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/cierres")
    suspend fun updateHistoryClosing(
        @Query("id") id: String,
        @Query("negocio_id") businessId: String,
        @Body body: RequestBody,
    ): ResponseBody

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/pagos")
    suspend fun updateHistoryPayment(
        @Query("cierre_id") closingId: String,
        @Query("metodo_id") methodId: String,
        @Body body: RequestBody,
    ): ResponseBody
}
