package com.example.mangiaebbasta.components.Order

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.homepage_progetto.viewModels.AppViewModel
import com.example.mangiaebbasta.R
import com.example.mangiaebbasta.components.LoadingScreen
import com.example.mangiaebbasta.model.CompletedOrderResponse
import com.example.mangiaebbasta.model.MenuResponseFromGet
import com.example.mangiaebbasta.model.OnDeliveryOrderResponse
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import kotlinx.coroutines.delay

@Composable
fun OrderPage(appViewModel: AppViewModel) {

    val order = appViewModel.orderInfo.collectAsState().value
    val userInfo = appViewModel.user.collectAsState().value
    var menu by remember { mutableStateOf(null as MenuResponseFromGet?) }
    val mapViewPortState = rememberMapViewportState()
    //problema, il delay è portato anche nella navigazione, quindi è lenta. Da risolvere
    LaunchedEffect(Unit) {
        Log.d("OrderPage", "Montata")
        appViewModel.getOrderInfo()
        appViewModel.setScreen("order")
    }
    LaunchedEffect(order) {

        if (order != null) {
            when (order) {
                is CompletedOrderResponse -> {
                    menu = appViewModel.getMenu(order.mid)
                    mapViewPortState.setCameraOptions {

                        center(Point.fromLngLat(order.deliveryLocation.lng, order.deliveryLocation.lat))
                        zoom(15.0)
                    }
                }
                is OnDeliveryOrderResponse -> {
                    menu = appViewModel.getMenu(order.mid)
                    mapViewPortState.setCameraOptions {
                        center(Point.fromLngLat(order.currentPosition.lng, order.currentPosition.lat))
                        zoom(15.0)
                    }
                    delay(5000)
                    appViewModel.getOrderInfo()
                }
            }
        }
    }
    if ((order is CompletedOrderResponse || order is OnDeliveryOrderResponse) && menu != null) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Order Page")

            if (order is CompletedOrderResponse) {
                Text(text = "Ordine Completo")
                Text("Consegnato alle: " + order.deliveryTimestamp)
                Text("Ordine numero: " + order.oid.toString())
                Text("Menu ordinato: " + menu!!.name)
            } else if (order is OnDeliveryOrderResponse) {
                Text(text = "Ordine in consegna")
                Text("Il drone arriverà alle: " + order.expectedDeliveryTimestamp)
                Text("Ordine numero: " + order.oid.toString())
                Text("Menu ordinato: " + menu!!.name)
            }
            MapboxMap(
                modifier = Modifier.fillMaxSize(),
                mapViewportState = mapViewPortState,
            ) {
                MapEffect (Unit) { mapView ->
                    mapView.mapboxMap.loadStyle("mapbox://styles/pizoz/cm5iifwft001z01s3dfhfbtak")
                }
                if (menu != null) {
                    when(order) {
                        is OnDeliveryOrderResponse -> {
                            val drone = rememberIconImage(
                                key = R.drawable.drone,
                                painter = painterResource(id = R.drawable.drone)
                            )
                            PointAnnotation(
                                point = Point.fromLngLat(
                                    order.currentPosition.lng,
                                    order.currentPosition.lat
                                )
                            ) {
                                iconImage = drone
                                iconSize = 0.2
                            }
                            val home = rememberIconImage(
                                key = R.drawable.home,
                                painter = painterResource(id = R.drawable.home)
                            )
                            PointAnnotation(
                                point = Point.fromLngLat(
                                    order.deliveryLocation.lng,
                                    order.deliveryLocation.lat
                                )
                            ) {
                                iconImage = home
                                iconSize = 0.2
                            }
                        }
                        is CompletedOrderResponse -> {
                            val home = rememberIconImage(
                                key = R.drawable.home,
                                painter = painterResource(id = R.drawable.home)
                            )
                            PointAnnotation(
                                point = Point.fromLngLat(
                                    order.deliveryLocation.lng,
                                    order.deliveryLocation.lat
                                )
                            ) {
                                iconImage = home
                                iconSize = 0.2
                            }

                        }
                    }

                    val shop = rememberIconImage(key = R.drawable.shop, painter = painterResource(id = R.drawable.shop))
                    PointAnnotation(point = Point.fromLngLat(menu!!.location.lng, menu!!.location.lat)) {
                        iconImage = shop
                        iconSize = 0.2
                    }
                }

            }
        }
    } else if (userInfo != null && order == null) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Order Page")
            Text(text = "Nessun ordine")
        }
    } else {
        Log.d("OrderPage", menu.toString())
        LoadingScreen()
    }
}