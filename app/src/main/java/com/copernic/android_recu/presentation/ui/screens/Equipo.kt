package com.copernic.android_recu.presentation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.copernic.android_recu.data.firebase.FirebaseService
import com.copernic.android_recu.model.Equipo
import com.copernic.android_recu.model.Liga
import com.copernic.android_recu.presentation.ui.theme.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun EquipoScreen(navController: NavController, firebaseService: FirebaseService) {
    EquipoBody(navController, firebaseService)
}

@Composable
fun EquipoBody(navController: NavController, firebaseService: FirebaseService) {

    val scope = rememberCoroutineScope()

    var equipos by remember { mutableStateOf<List<Equipo>>(emptyList()) }
    var ligas by remember { mutableStateOf<Map<String, Liga>>(emptyMap()) }
    var cargando by remember { mutableStateOf(true) }

    var filtro by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        scope.launch {
            val snapEquipos = firebaseService.db.collection("equipos").get().await()
            equipos = snapEquipos.toObjects(Equipo::class.java)

            val snapLigas = firebaseService.db.collection("ligas").get().await()
            ligas = snapLigas.toObjects(Liga::class.java).associateBy { it.id }

            cargando = false
        }
    }

    val equiposFiltrados = equipos.filter {
        it.nombre.contains(filtro, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FootballWhite)
    ) {

        RecuHeader(title = "Equipos")

        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {

            OutlinedTextField(
                value = filtro,
                onValueChange = { filtro = it },
                label = { Text("Buscar equipo por nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            if (cargando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (equiposFiltrados.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontraron equipos.")
                }
            } else {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(equiposFiltrados) { equipo ->
                        val ligaNombre = ligas[equipo.ligaId]?.nombre ?: "Liga desconocida"

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            colors = CardDefaults.cardColors(FootballGray)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {

                                // IMAGEN + NOMBRE
                                Row(verticalAlignment = Alignment.CenterVertically) {

                                    Image(
                                        painter = rememberAsyncImagePainter(equipo.imagenUrl),
                                        contentDescription = equipo.nombre,
                                        modifier = Modifier
                                            .size(80.dp)
                                            .padding(end = 12.dp),
                                        contentScale = ContentScale.Crop
                                    )

                                    Column {
                                        Text(
                                            text = equipo.nombre,
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = FootballWhite
                                            )
                                        )
                                        Text(
                                            text = ligaNombre,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = FootballWhite
                                            )
                                        )
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                // DESCRIPCIÓN
                                Text("Descripción:", fontWeight = FontWeight.Bold, color = FootballWhite)
                                Text(equipo.descripcion, color = FootballWhite)

                                Spacer(Modifier.height(10.dp))

                                // FECHA
                                val fecha = remember(equipo.fechaCreacion) {
                                    java.text.SimpleDateFormat(
                                        "dd/MM/yyyy HH:mm",
                                        java.util.Locale.getDefault()
                                    ).format(java.util.Date(equipo.fechaCreacion))
                                }

                                Text("Fecha de creación: $fecha", color = FootballWhite)

                                // AUTOR
                                Text("Autor ID: ${equipo.autorId}", color = FootballWhite)

                                // GPS
                                Text("Ubicación: ${equipo.latitud}, ${equipo.longitud}", color = FootballWhite)
                            }
                        }
                    }
                }
            }
        }

        RecuFooterPostLogin(navController)
    }
}
