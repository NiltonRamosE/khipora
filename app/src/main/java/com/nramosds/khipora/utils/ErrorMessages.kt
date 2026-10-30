package com.nramosds.khipora.utils

import com.google.gson.JsonParser
import com.nramosds.khipora.domain.exception.ClosingSaveException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import retrofit2.HttpException

/** Converts service errors into messages the person using the app can act on. */
internal fun userErrorMessage(error: Throwable): String {
    return when (error) {
        is ClosingSaveException -> {
            if (error.requiresReview) {
                "El cierre se registró, pero no pudimos confirmar todos sus pagos. " +
                    "Revisa tus registros antes de volver a guardarlo para evitar duplicados."
            } else {
                "No se guardó el cierre porque hubo un problema con los pagos. " +
                    (error.cause?.let(::userErrorMessage) ?: "Vuelve a intentarlo.")
            }
        }

        is UnknownHostException ->
            "No pudimos conectarnos. Revisa tu conexión a Internet y vuelve a intentarlo."

        is SocketTimeoutException ->
            "La respuesta está tardando más de lo esperado. " +
                "Si estabas guardando, revisa tus registros antes de intentarlo de nuevo."

        is SSLException ->
            "No pudimos establecer una conexión segura. Revisa tu conexión y vuelve a intentarlo."

        is HttpException -> httpErrorMessage(error)
        is IOException ->
            "Se interrumpió la conexión. Si estabas guardando, " +
                "revisa tus registros antes de volver a intentarlo."

        else -> "No pudimos completar la acción. Vuelve a intentarlo en unos momentos."
    }
}

private fun httpErrorMessage(error: HttpException): String {
    val databaseCode = try {
        val body = error.response()?.errorBody()?.string().orEmpty()
        JsonParser.parseString(body).asJsonObject.get("code")?.asString
    } catch (_: Exception) {
        null
    }

    return when {
        databaseCode == "23505" ->
            "Ya existe un registro con estos datos. Revisa la información antes de volver a guardar."

        databaseCode == "23503" ->
            "Uno de los datos seleccionados ya no está disponible. " +
                "Vuelve a cargar la pantalla y revisa tu selección."

        error.code() == 409 ->
            "Ya existe un registro con estos datos. Revisa la información antes de volver a guardar."

        error.code() == 401 || error.code() == 403 ->
            "No tienes permiso para realizar esta acción. " +
                "Contacta con quien administra el negocio."

        error.code() == 404 ->
            "No encontramos la información que necesitas. Vuelve a cargar la pantalla."

        error.code() == 429 ->
            "Hay muchas solicitudes en este momento. Espera un poco y vuelve a intentarlo."

        error.code() >= 500 ->
            "El servicio no está disponible por el momento. Inténtalo nuevamente más tarde."

        else -> "No pudimos guardar los cambios. Revisa los datos e inténtalo nuevamente."
    }
}
