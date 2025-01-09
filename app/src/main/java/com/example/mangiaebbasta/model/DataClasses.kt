package com.example.mangiaebbasta.model

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
data class UserResponseFromCreate(
    val sid: String,
    val uid: Int
)

@Serializable
data class ResponseError(val message: String)

@Serializable
data class Posizione(
    val lat: Double,
    val lng: Double
) {
    override fun toString(): String {
        return "{lat: $lat, lng: $lng}"
    }
}

@Serializable
data class UserResponseFromGet(
    val firstName: String?,
    val lastName: String?,
    val cardFullName: String?,
    val cardNumber: String?,
    val cardExpireMonth: Int?,
    val cardExpireYear: Int?,
    val cardCVV: String?,
    val uid: Int,
    val lastOid: Int?,
    val orderStatus: String?,
)


@Serializable
data class UserForPut(
    val firstName: String?,
    val lastName: String?,
    val cardFullName: String?,
    val cardNumber: String?,
    val cardExpireMonth: Int?,
    val cardExpireYear: Int?,
    val cardCVV: String?,
    val sid: String,
)

@Serializable
data class OrderResponseFromGet(
    val oid: Int,
    val mid: Int,
    val uid: Int,
    val creationTimestamp: String,
    val status: String,
    val deliveryLocation: Posizione,
    val deliveryTimestamp: String?,
    val expectedDeliveryTimestamp: String?,
    val currentPosition: Posizione,
)

@Serializable
data class DeliveryLocationWithSid(
    val sid: String,
    val deliveryLocation: Posizione
)

@Serializable
data class OnDeliveryOrderResponse(
    val oid: Int,
    val mid: Int,
    val uid: Int,
    val creationTimestamp: String,
    val status: String,
    val deliveryLocation: Posizione,
    val currentPosition: Posizione,
    val expectedDeliveryTimestamp: String,
)

@Serializable
data class CompletedOrderResponse(
    val oid: Int,
    val mid: Int,
    val uid: Int,
    val creationTimestamp: String,
    val status: String,
    val deliveryLocation: Posizione,
    val deliveryTimestamp: String,
    val currentPosition: Posizione,
)

@Serializable
data class OrderStatusResponse(
    val status: String
)


@Serializable
data class NearMenu(
    val mid: Int,
    val name: String,
    val price: Double,
    val location: Posizione,
    val imageVersion: Int,
    val shortDescription: String,
    val deliveryTime: Int,
)


@Serializable
data class MenuResponseFromGet(
    val mid: Int,
    val name: String,
    val price: Double,
    val location: Posizione,
    val imageVersion: Int,
    val shortDescription: String,
    val deliveryTime: Int,
    val longDescription: String,
)

@Serializable
data class MenuImageResponse(
    val base64: String
)

@Entity(tableName = "images")
data class


ImageForDB(
    @PrimaryKey val mid: Int,
    val imageVersion: Int,
    val base64: String,
)


data class NearMenuandImage(
    val nearMenu: NearMenu,
    val image: Bitmap
)

data class MenuResponseFromGetandImage(
    val menuResponseFromGet: MenuResponseFromGet,
    val image: Bitmap
)
