package com.example.homepage_progetto.model

import android.location.Location
import android.net.Uri
import android.util.Log
import com.example.mangiaebbasta.model.CompletedOrderResponse
import com.example.mangiaebbasta.model.DeliveryLocationWithSid
import com.example.mangiaebbasta.model.MenuImageResponse
import com.example.mangiaebbasta.model.MenuResponseFromGet
import com.example.mangiaebbasta.model.NearMenu
import com.example.mangiaebbasta.model.OnDeliveryOrderResponse
import com.example.mangiaebbasta.model.OrderStatusResponse
import com.example.mangiaebbasta.model.UserForPut
import com.example.mangiaebbasta.model.UserResponseFromCreate
import com.example.mangiaebbasta.model.UserResponseFromGet

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object CommunicationController {

    private val BASE_URL = "https://develop.ewlab.di.unimi.it/mc/2425"
    var sid: String? = null
    private val TAG = " CommunicationController"

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    enum class HttpMethod {
        GET,
        POST,
        DELETE,
        PUT
    }


    suspend fun genericRequest(
        url: String, method: HttpMethod,
        queryParameters: Map<String, String> = emptyMap(),
        requestBody: Any? = null
    ): HttpResponse {

        val urlUri = Uri.parse(url)
        val urlBuilder = urlUri.buildUpon()
        queryParameters.forEach { (key, value) ->
            urlBuilder.appendQueryParameter(key, value.toString())
        }
        val completeUrlString = urlBuilder.build().toString()
        Log.d(TAG, completeUrlString)

        val request: HttpRequestBuilder.() -> Unit = {
            requestBody?.let {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
        }

        val result = when (method) {
            HttpMethod.GET -> client.get(completeUrlString, request)
            HttpMethod.POST -> client.post(completeUrlString, request)
            HttpMethod.DELETE -> client.delete(completeUrlString, request)
            HttpMethod.PUT -> client.put(completeUrlString, request)
        }
        return result
    }

    suspend fun createUser(): UserResponseFromCreate {
        Log.d(TAG, "createUser")

        val url = BASE_URL + "/user"

        val httpResponse = genericRequest(url, HttpMethod.POST)
        if (!httpResponse.status.isSuccess()) {
            throw Exception("Errore nella creazione dell'utente: ${httpResponse.status}")
        }
        val result: UserResponseFromCreate = httpResponse.body()
        return result
    }

    suspend fun getUserInfo(UserResponseFromCreate: UserResponseFromCreate): UserResponseFromGet {
        Log.d(TAG, "getUserInfo")

        val url = BASE_URL + "/user" + "/" + UserResponseFromCreate.uid

        val httpResponse =
            genericRequest(url, HttpMethod.GET, mapOf("sid" to UserResponseFromCreate.sid))
        if (!httpResponse.status.isSuccess()) {
            throw Exception("Errore nel reperimento dei dati dell'utente: ${httpResponse.status}")
        }
        val result: UserResponseFromGet = httpResponse.body()
        return result
    }

    suspend fun updateUser(UserForPut: UserForPut, uid: Int): Unit {
        Log.d(TAG, "updateUser")

        val url = BASE_URL + "/user" + "/" + uid

        val httpResponse = genericRequest(url, HttpMethod.PUT, requestBody = UserForPut)
        if (!httpResponse.status.isSuccess()) {
            throw Exception("Errore nell'aggiornamento dei dati dell'utente: ${httpResponse.status}")
        }

    }

    suspend fun createOrder(DeliveryLocationWithSid: DeliveryLocationWithSid, mid: Int): Any {
        Log.d(TAG, "createOrder")

        val url = "$BASE_URL/menu/$mid/buy"

        try {
            val httpResponse = genericRequest(url, HttpMethod.POST, requestBody = DeliveryLocationWithSid)

            if (httpResponse.status.value == 409) {
                Log.w(TAG, "User already has an active order")
                throw Exception("User already has an active order")
            }

            if (!httpResponse.status.isSuccess()) {
                throw Exception("Error creating order: ${httpResponse.status}")
            }

            val partialResult: OrderStatusResponse = httpResponse.body()
            return when (partialResult.status) {
                "ON_DELIVERY" -> httpResponse.body<OnDeliveryOrderResponse>()
                "COMPLETED" -> httpResponse.body<CompletedOrderResponse>()
                else -> throw Exception("Unexpected response type")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in createOrder", e)
            throw e
        }
    }

    suspend fun getOrder(oid: Int, sid: String): Any {
        Log.d(TAG, "getOrder")

        val url = BASE_URL + "/order" + "/" + oid

        val httpResponse = genericRequest(url, HttpMethod.GET, mapOf("sid" to sid))
        if (!httpResponse.status.isSuccess()) {
            throw Exception("Errore nel reperimento dei dati dell'ordine: ${httpResponse.status}")
        }
        val partialResult: OrderStatusResponse = httpResponse.body()
        if (partialResult.status == "ON_DELIVERY") {
            val result: OnDeliveryOrderResponse = httpResponse.body()
            return result
        } else {
            val result: CompletedOrderResponse = httpResponse.body()
            return result
        }

    }

    suspend fun getMenu(mid: Int, sid: DeliveryLocationWithSid): MenuResponseFromGet {
        Log.d(TAG, "getMenu")
        val url = BASE_URL + "/menu" + "/" + mid

        val httpResponse = genericRequest(
            url,
            HttpMethod.GET,
            mapOf(
                "sid" to sid.sid,
                "lat" to sid.deliveryLocation.lat.toString(),
                "lng" to sid.deliveryLocation.lng.toString()
            )
        )
        if (!httpResponse.status.isSuccess()) {
            throw Exception("Errore nel reperimento dei dati del menu: ${httpResponse.status}")
        }
        val result: MenuResponseFromGet = httpResponse.body()
        Log.d(TAG, "getMenu: $result")
        return result
    }

    suspend fun getMenus(sid: DeliveryLocationWithSid): List<NearMenu> {

        Log.d(TAG, "getMenus")

        val url = BASE_URL + "/menu"

        val httpResponse = genericRequest(
            url,
            HttpMethod.GET,
            mapOf(
                "sid" to sid.sid,
                "lat" to sid.deliveryLocation.lat.toString(),
                "lng" to sid.deliveryLocation.lng.toString()
            )
        )
        if (!httpResponse.status.isSuccess()) {
            throw Exception("Errore nel reperimento dei dati dei menu: ${httpResponse.status}")
        }
        val result: List<NearMenu> = httpResponse.body()
        Log.d(TAG, "getMenus: $result")
        return result
    }

    suspend fun getMenuImage(mid: Int, sid: String): MenuImageResponse? {
        Log.d(TAG, "getMenuImage: Starting request for menu $mid")
        val url = "$BASE_URL/menu/$mid/image"

        return try {
            val httpResponse = genericRequest(
                url,
                HttpMethod.GET,
                mapOf("sid" to sid)
            )

            Log.d(TAG, "getMenuImage: Received response with status ${httpResponse.status}")

            if (httpResponse.status.isSuccess()) {
                val result: MenuImageResponse = httpResponse.body()
                Log.d(
                    TAG,
                    "getMenuImage: Successfully parsed response. Image data length: ${result.base64.length}"
                )
                result
            } else {
                Log.e(TAG, "getMenuImage: Failed to retrieve image. Status: ${httpResponse.status}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "getMenuImage: Exception occurred", e)
            null
        }
    }


}

