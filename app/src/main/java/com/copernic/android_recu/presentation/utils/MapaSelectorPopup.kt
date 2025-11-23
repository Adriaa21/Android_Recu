package com.copernic.android_recu.presentation.utils

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun MapaSelectorPopup(
    latitudInicial: Double,
    longitudInicial: Double,
    onDismiss: () -> Unit,
    onSelect: (Double, Double) -> Unit
) {
    var latitud by remember { mutableStateOf(latitudInicial) }
    var longitud by remember { mutableStateOf(longitudInicial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar ubicación") },
        text = {
            Column {
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(
                        LatLng(latitud, longitud), 14f
                    )
                }

                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng ->
                        latitud = latLng.latitude
                        longitud = latLng.longitude
                    }
                ) {
                    Marker(
                        state = MarkerState(position = LatLng(latitud, longitud)),
                        title = "Ubicación seleccionada"
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text("Latitud: $latitud")
                Text("Longitud: $longitud")
            }
        },
        confirmButton = {
            Button(onClick = { onSelect(latitud, longitud) }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
