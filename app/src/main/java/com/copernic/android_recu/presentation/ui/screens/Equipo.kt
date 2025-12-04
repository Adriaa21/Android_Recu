package com.copernic.android_recu.presentation.ui.screens

import android.content.Intent
import android.net.Uri
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
import com.copernic.android_recu.model.User
import com.copernic.android_recu.presentation.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Pantalla principal de equipos
@Composable
fun EquipoScreen(navController: NavController, firebaseService: FirebaseService) {
    EquipoBody(navController, firebaseService)
}

// Contenido principal de la pantalla
@Composable
fun EquipoBody(navController: NavController, firebaseService: FirebaseService) {

    val scope = rememberCoroutineScope()

    // Estados de datos
    var equipos by remember { mutableStateOf<List<Equipo>>(emptyList()) }
    var ligas by remember { mutableStateOf<Map<String, Liga>>(emptyMap()) }
    var usuarios by remember { mutableStateOf<Map<String, User>>(emptyMap()) }
    var cargando by remember { mutableStateOf(true) }
    var filtro by remember { mutableStateOf("") }

    // Cargar datos desde Firebase
    LaunchedEffect(Unit) {
        scope.launch {
            val snapEquipos = firebaseService.db.collection("equipos").get().await()
            equipos = snapEquipos.toObjects(Equipo::class.java)

            val snapLigas = firebaseService.db.collection("ligas").get().await()
            ligas = snapLigas.toObjects(Liga::class.java).associateBy { it.id }

            val snapUsuarios = firebaseService.db.collection("users").get().await()
            usuarios = snapUsuarios.toObjects(User::class.java).associateBy { it.id }

            cargando = false
        }
    }

    // Filtro por nombre del equipo
    val equiposFiltrados = equipos.filter {
        it.nombre.contains(filtro, ignoreCase = true)
    }

    // Estructura general de la pantalla
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FootballWhite)
    ) {

        // Cabecera superior
        RecuHeader(title = "Equipos")

        // Zona central con peso para no tapar el footer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
        ) {

            // Campo de búsqueda
            OutlinedTextField(
                value = filtro,
                onValueChange = { filtro = it },
                label = { Text("Buscar equipo por nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Mostrar loading, mensaje vacío o lista
            if (cargando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (equiposFiltrados.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontraron equipos.")
                }
            } else {

                // Lista de equipos
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(equiposFiltrados) { equipo ->

                        val ligaNombre = ligas[equipo.ligaId]?.nombre ?: "Liga desconocida"
                        val autorUsername = usuarios[equipo.autorId]?.username ?: "Desconocido"

                        // Tarjeta de equipo
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            colors = CardDefaults.cardColors(FootballGray)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {

                                // Imagen y nombre
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
                                        Text(text = ligaNombre, color = FootballWhite)
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                // Descripción
                                Text("Descripción:", fontWeight = FontWeight.Bold, color = FootballWhite)
                                Text(equipo.descripcion, color = FootballWhite)

                                Spacer(Modifier.height(10.dp))

                                // Fecha de creación
                                val fecha = remember(equipo.fechaCreacion) {
                                    java.text.SimpleDateFormat(
                                        "dd/MM/yyyy HH:mm",
                                        java.util.Locale.getDefault()
                                    ).format(java.util.Date(equipo.fechaCreacion))
                                }
                                Text("Fecha de creación: $fecha", color = FootballWhite)

                                // Autor del equipo
                                Text("Autor: $autorUsername", color = FootballWhite)

                                Spacer(Modifier.height(8.dp))

                                // Botón para abrir la ubicación en Google Maps
                                Button(
                                    onClick = {
                                        val gmmIntentUri = Uri.parse(
                                            "geo:${equipo.latitud},${equipo.longitud}?q=${equipo.latitud},${equipo.longitud}(${equipo.nombre})"
                                        )
                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                        mapIntent.setPackage("com.google.android.apps.maps")
                                        navController.context.startActivity(mapIntent)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(FootballGreen)
                                ) {
                                    Text("Ver ubicación en Maps", color = FootballWhite)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Footer con navegación inferior
        RecuFooterPostLogin(navController)
    }
}
